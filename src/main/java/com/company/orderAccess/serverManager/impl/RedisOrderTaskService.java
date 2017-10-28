package com.company.orderAccess.serverManager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.company.orderTask.domain.OrderTaskRunInfo;
@Service("redisOrderTaskService")
public class RedisOrderTaskService {
	
	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	
	@Value("${order.task.queneName:orderTaskQuene}")
	private String orderTaskQueneName;
	/**
	 * 任务队列que的redis的key的前缀
	 */
	private final String  Prefix_key_OrderTaskQuene = "orderQuene:";
	
	private final String  Prefix_key_OrderTaskQueneLock = "OrderQLock:";
	
	/**
	 * 创建
	 * @param queneName
	 * @return
	 */
	public String getOrderTaskQueneKey(String queneName,String category,String step)
	{
		return Prefix_key_OrderTaskQuene  + category  + ":" +queneName+ ":" + step;
	}
	
	/**
	 * 从队列中获取锁的key
	 * @param queneName
	 * @param category
	 * @param step
	 * @return
	 */
	public String getOrderTaskLockKey(String queneName,String category,String step)
	{
		return Prefix_key_OrderTaskQueneLock  + category  + ":" +queneName+ ":" + step;
	}
	/**
	 * 获取订单任务重做key
	 * @param queneName
	 * @param category
	 * @param step
	 * @return
	 */
	public String getOrderTaskRedoKey(String queneName,String category,String step)
	{
		return  "OrderQRedo:" + category  + ":" +queneName+ ":" + step;
	}
	
	public String getOrderTaskKey(OrderTaskRunInfo orderTaskInfo)
	{
		return "orderTask"  +orderTaskInfo.getCurrentStep()  + ":" +orderTaskInfo.getFlowId() + ":" + orderTaskInfo.getExpireTime();
	}
	
	/**
	 * 将订单任务放入队列
	 * @param orderTaskInfo
	 * @return
	 */
    public boolean putOrderTaskToQuene(OrderTaskRunInfo orderTaskInfo)
    {
    	String key = this.getOrderTaskQueneKey(orderTaskQueneName,orderTaskInfo.getCatetory(),orderTaskInfo.getCurrentStep());
    	this.redisTemplate.opsForList().rightPush(key, orderTaskInfo);
		return true;
    }
    
    /**
     * 如果任务成功，删除调度队列中的重做任务；
     * @param orderTaskRunInfo
     * @return
     */
    public boolean delRedoTask(OrderTaskRunInfo orderTaskRunInfo)
    {
    	try {
			String keyOrderRedo = getOrderTaskRedoKey(orderTaskQueneName,orderTaskRunInfo.getCatetory(),orderTaskRunInfo.getCurrentStep());
			redisTemplate.opsForHash().delete(keyOrderRedo, getOrderTaskKey(orderTaskRunInfo));
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    /**
     * 重做超时调度的任务
     * @param orderTaskInfo
     * @return
     */
    public boolean redoTimeoutTask(String category,String step,int numbers)
    {
    	String key = this.getOrderTaskQueneKey(orderTaskQueneName,category,step);
    	String keyOrderRedo = getOrderTaskRedoKey(orderTaskQueneName,category,step);
    	List<String> timeOutRedoList = new ArrayList<String>();
		Map<Object, Object> redoMap = redisTemplate.opsForHash().entries(keyOrderRedo);
		int i=0;
		for (Map.Entry<Object, Object> entry : redoMap.entrySet()) {
			i++;
			if(i>=numbers)
			{
				break;
			}
			try {
				OrderTaskRunInfo orderTaskRunInfo =(OrderTaskRunInfo)entry.getValue();
				if(orderTaskRunInfo!=null && System.currentTimeMillis() - orderTaskRunInfo.getExpireTime()<0)
				{
					this.redisTemplate.opsForList().rightPush(key, orderTaskRunInfo);
				}
				else if(orderTaskRunInfo!=null)
				{
					timeOutRedoList.add(getOrderTaskKey(orderTaskRunInfo));
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//删除超时的任务
		for( i=0;i<timeOutRedoList.size();i++)
		{
			redisTemplate.opsForHash().delete(keyOrderRedo, timeOutRedoList.get(i));
		}
		timeOutRedoList.clear();
		return true;
    }
    
    /**
     * 从队列中获取需要调度的任务
     * @param category -- 订单类型
     * @param step   -- 步骤
     * @param numbers  -- 获取多少个任务
     * @return  --null -not get lock
     */
    public List<OrderTaskRunInfo> schedulerOrderTaskToQuene(String category,String step,int numbers)
    {
    	long requestTime = 0;
    	List<OrderTaskRunInfo> retLists= new ArrayList<OrderTaskRunInfo>();
        String lockKey = getOrderTaskLockKey(orderTaskQueneName,category,step);
    	//全局加锁
    	try {
    		requestTime = this.getCommonLock(lockKey);
			if(requestTime==0)
			{
				//没有抢到锁，返回空
				return null;
			}
			
			String key = this.getOrderTaskQueneKey(orderTaskQueneName,category,step);
			String keyOrderRedo = getOrderTaskRedoKey(orderTaskQueneName,category,step);
			
			//获取多个任务
			for(int i=0;i<numbers;i++)
			{
				//从队列任务中获取
				OrderTaskRunInfo orderTaskInfo = (OrderTaskRunInfo)this.redisTemplate.opsForList().index(key, 0);
				if(orderTaskInfo==null)
				{
					break;
				}
				//判断调度任务是否过期
				if(System.currentTimeMillis() - orderTaskInfo.getExpireTime()<0)
				{
					this.redisTemplate.opsForList().leftPop(key);
					if(i>0)
					{
						i--;
					}
					continue;
				}
				retLists.add(orderTaskInfo);
				//放入到重做MAP中；
				try {
					Map<Object, Object> map =  redisTemplate.opsForHash().entries(keyOrderRedo); 
					OrderTaskRunInfo redoOrderTaskInfo = orderTaskInfo.clone();
					if(redoOrderTaskInfo!=null)
					{
						redoOrderTaskInfo.setRunTime(System.currentTimeMillis());
						int runTimes = redoOrderTaskInfo.getRuntimes();
						runTimes++;
						redoOrderTaskInfo.setRuntimes(runTimes);
						map.put(this.getOrderTaskKey(redoOrderTaskInfo), redoOrderTaskInfo);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//从队列中移走
				this.redisTemplate.opsForList().leftPop(key);
				
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally {
    		this.releaseCommonLock(lockKey, requestTime);
    	}
    	return retLists;
    }
    
    /**
	 * 申请通用锁，等待30秒
	 * @param lockKey
	 * @return  0--失败
	 */
	public long  getCommonLock(String lockKey){  
		try{  
            
            long startTime = System.currentTimeMillis();  
            boolean needWait = false;
            while (true){
            	{
	            	if(redisTemplate.opsForValue().setIfAbsent(lockKey,String.valueOf(startTime))){  
	            		startTime = System.currentTimeMillis(); 
	            		redisTemplate.opsForValue().set(lockKey,String.valueOf(startTime),30,TimeUnit.SECONDS);  
	                	return startTime;  
	                }
	            	//如果是第一次进来，最好判断一下是否老的已经过期，否则会死锁
	            	else if(!needWait)
	            	{
	            		needWait = true;
	            		String requestTimeS = (String)redisTemplate.opsForValue().get(lockKey);
	            		//获取时间
	            		if(requestTimeS!=null)
	            		{
		            		long requestTimeL=0;
							try {
								requestTimeL = Long.parseLong(requestTimeS);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		            		//如果没有获取到，并且已经超时
		                    if(System.currentTimeMillis() - requestTimeL > 30000){  
		                    	redisTemplate.delete(lockKey);
		                    	continue;
		                    }		
		                    else
		                    {
		                    	needWait = true;	
		                    }
	            		}
	            		//如果没有时间
	            		else
	            		{
	            			redisTemplate.delete(lockKey);
		                    continue;
	            		}
	            	}
            	}
                //如果没有获取到，并且已经超时
                if(System.currentTimeMillis() - startTime > 30000){  
                    return 0;  
                }  
                //延迟一段时间
                Thread.sleep(1000);  
            }  
        }catch (Exception e){  
              e.printStackTrace();
            return 0;  
        }  
        
    }

	public void releaseCommonLock(String lockKey,long requestTime)  
	{
		try {
			String requestTimeS = (String)redisTemplate.opsForValue().get(lockKey);
			long requestTimeL = Long.parseLong(requestTimeS);
			if(requestTimeL==requestTime)
			{
				redisTemplate.delete(lockKey);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
