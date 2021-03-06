package com.company.orderAccess.serverManager.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.company.orderTask.domain.OrderTaskRunInfo;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;
@Service("redisOrderTaskService")
public class RedisOrderTaskService {
	
	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	
	@Value("${order.task.queneName:orderTaskQuene}")
	private String orderTaskQueneName;
	
	@Value("${order.cache.expireHours:120}")
	private int orderCacheExpireHours;
	
	/**
	 * 任务队列que的redis的key的前缀
	 */
	private final String  Prefix_key_OrderTaskQuene = "orderQuene:";
	
	private final String  Prefix_key_OrderTaskQueneLock = "OrderQLock:";
	private Logger log = LoggerFactory.getLogger(getClass());
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
	 * 
	 * @param queneName
	 * @param category
	 * @param step
	 * @return
	 */
	public String getOrderRedoLockKey(String queneName,String category,String step)
	{
		/*
		if(true)
		{
			return getOrderTaskLockKey(queneName,category,step);
		}
		*/
		return Prefix_key_OrderTaskQueneLock + "redo:" + category  + ":" +queneName+ ":" + step;
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
		return  "OrderQRedo:" + category  + "_" +queneName+ ":" + step;
	}
	
	public String getOrderTaskKey(OrderTaskRunInfo orderTaskInfo)
	{
		return "orderTask:"  + orderTaskInfo.getOrderId() + ":" + orderTaskInfo.getCurrentStep()  + ":" +orderTaskInfo.getFlowId() + ":" + orderTaskInfo.getExpireTime();
	}
	
	/**
	 * 将订单任务放入队列
	 * @param orderTaskInfo
	 * @return
	 */
    public boolean putOrderTaskToQuene(OrderTaskRunInfo orderTaskInfo)
    {
    	String key = this.getOrderTaskQueneKey(orderTaskQueneName,orderTaskInfo.getCatetory(),orderTaskInfo.getCurrentStep());
    	this.redisTemplate.opsForList().rightPush(key, serializeRedis(orderTaskInfo));
		return true;
    }
    
    private String serializeRedis(Object t)
    {
    	return JsonUtil.toJson(t);
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
			log.debug("del orderTask:" + orderTaskRunInfo.toString());
			redisTemplate.opsForHash().delete(keyOrderRedo, getOrderTaskKey(orderTaskRunInfo));
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    /**
     * 更新重做日志的信息，等待下一次调度
     * @param orderTaskRunInfo
     * @return
     */
    public boolean updateRedoTask(OrderTaskRunInfo orderTaskRunInfo)
    {
    	try {
			String keyOrderRedo = getOrderTaskRedoKey(orderTaskQueneName,orderTaskRunInfo.getCatetory(),orderTaskRunInfo.getCurrentStep());
			log.debug("update orderTask:" + orderTaskRunInfo.toString());
			
			redisTemplate.opsForHash().put(keyOrderRedo, getOrderTaskKey(orderTaskRunInfo),orderTaskRunInfo);
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
    public boolean redoTimeoutTask(String category,String step,int numbers,int timeoutMills,int retryTimes)
    {
    	
    	String lockKey = getOrderRedoLockKey(orderTaskQueneName,category,step);
    	long lockTime = 0;
    	try {
    		lockTime = this.getCommonLock(lockKey, 60);
	    	if(lockTime == 0)
	    	{
	    		log.debug("redo task:get lock error!");				
	    		return false;
	    	}
	    	String key = this.getOrderTaskQueneKey(orderTaskQueneName,category,step);
	    	String keyOrderRedo = getOrderTaskRedoKey(orderTaskQueneName,category,step);
	    	List<String> timeOutRedoList = new ArrayList<String>();
			Map<Object, Object> redoMap = redisTemplate.opsForHash().entries(keyOrderRedo);
			int i=0;
			for (Map.Entry<Object, Object> entry : redoMap.entrySet()) {
				i++;
				if(i>=numbers)
				{
					log.debug("redo Task:exit:" + i+":" + numbers);
					break;
				}
				try {
					OrderTaskRunInfo orderTaskRunInfo=null;
					try {
						
						String str = (String)entry.getValue();
						  orderTaskRunInfo = JsonUtil.fromJson(str, OrderTaskRunInfo.class);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						continue;
					}
					log.debug("redo task:" + orderTaskRunInfo.toString());
					if(orderTaskRunInfo!=null)
					{
						int havedRetryTimes = orderTaskRunInfo.getRuntimes();
						//判断是否重试次数已经大于规定的重试次数，如果大于，延迟重做
						if(havedRetryTimes>retryTimes)
						{
							timeoutMills = ((int)(havedRetryTimes/retryTimes)) * timeoutMills;
						}
						//如果时间已经过期,放入重做队列，会通知运维任务过期
						if(orderTaskRunInfo.getExpireTime()>0 && System.currentTimeMillis() - orderTaskRunInfo.getExpireTime()>=0)
						{
							this.redisTemplate.opsForList().rightPush(key, this.serializeRedis(orderTaskRunInfo));
							timeOutRedoList.add((String)entry.getKey());
							log.debug("redo task: add to timeout list key:" + orderTaskRunInfo.toString());
							
						}
						//判断是否时间超时需要重做
						else if(System.currentTimeMillis() - orderTaskRunInfo.getRunTime() >=timeoutMills)
						{
							long size = this.redisTemplate.opsForList().rightPush(key, serializeRedis(orderTaskRunInfo));
							timeOutRedoList.add((String)entry.getKey());
							log.debug("redo task: need to redo:" + size + ":" + orderTaskRunInfo.toString());
						}
						
					}
					else
					{
						try {
							timeOutRedoList.add(entry.getKey().toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String errorStr = errors.toString();
					log.error(errorStr);
				}
			}
			//删除重做的任务
			for( i=0;i<timeOutRedoList.size();i++)
			{
				redisTemplate.opsForHash().delete(keyOrderRedo, timeOutRedoList.get(i));
				log.debug("redo task: delete :" + keyOrderRedo + "_" + timeOutRedoList.get(i));

			}
			timeOutRedoList.clear();
    	}
    	catch (Exception e)
    	{
    		StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String errorStr = errors.toString();
			log.error(errorStr);
    	}
    	finally {
    		if(lockTime>0)
    		{
    			this.releaseCommonLock(lockKey, lockTime);
    		}
    	}
			return true;
    }
    
    /**
     * 从队列中获取需要调度的任务
     * @param category -- 订单类型
     * @param step   -- 步骤
     * @param numbers  -- 获取多少个任务
     * @return  --null -not get lock
     */
    public List<OrderTaskRunInfo> getSchedulerOrderTasks(String category,String step,int numbers)
    {
    	long requestTime = 0;
    	List<OrderTaskRunInfo> retLists= new ArrayList<OrderTaskRunInfo>();
        String lockKey = getOrderTaskLockKey(orderTaskQueneName,category,step);
    	//全局加锁
    	try {
    		requestTime = this.getCommonLock(lockKey,30);
			if(requestTime==0)
			{
				//没有抢到锁，返回空
				log.debug("get orderTask lock is error!");
				
				return null;
			}
			
			String key = this.getOrderTaskQueneKey(orderTaskQueneName,category,step);
			String keyOrderRedo = getOrderTaskRedoKey(orderTaskQueneName,category,step);
			
			//获取多个任务
			for(int i=0;i<numbers;i++)
			{
				//从队列任务中获取
				OrderTaskRunInfo orderTaskInfo=null;
				try {
					String str = (String)this.redisTemplate.opsForList().index(key, 0);
					orderTaskInfo = JsonUtil.fromJson(str, OrderTaskRunInfo.class);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}
				if(orderTaskInfo==null)
				{
					log.debug("get orderTask is null");
					
					break;
				}
				//判断调度任务是否过期
				/**
				 * 不需要做，超时任务交给任务调度器，可以通知运维进行处理
				if(System.currentTimeMillis() - orderTaskInfo.getExpireTime()<0)
				{
					this.redisTemplate.opsForList().leftPop(key);
					if(i>0)
					{
						i--;
					}
					continue;
				}
				**/
				retLists.add(orderTaskInfo);
				//放入到重做MAP中；
				try {
					//Map<Object, Object> map =  redisTemplate.opsForHash().entries(keyOrderRedo); 
					OrderTaskRunInfo redoOrderTaskInfo = orderTaskInfo.clone();
					if(redoOrderTaskInfo!=null)
					{
						redoOrderTaskInfo.setRunTime(System.currentTimeMillis());
						int runTimes = redoOrderTaskInfo.getRuntimes();
						runTimes++;
						redoOrderTaskInfo.setRuntimes(runTimes);
						log.debug("get orderTask start put :" + keyOrderRedo + ":" + getOrderTaskKey(redoOrderTaskInfo) + ":" + redoOrderTaskInfo.toString());
						redisTemplate.opsForHash().put(keyOrderRedo, getOrderTaskKey(redoOrderTaskInfo), serializeRedis(redoOrderTaskInfo));
						log.debug("get orderTask end put :" + keyOrderRedo + ":" + getOrderTaskKey(redoOrderTaskInfo) + ":" + redoOrderTaskInfo.toString());
						
						//map.put(this.getOrderTaskKey(redoOrderTaskInfo), redoOrderTaskInfo);
					}
					else
					{
						
						this.log.error("put redo null :" + orderTaskInfo.toString());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					String errorStr = errors.toString();
					this.log.error(errorStr.toString());
				}
				//从队列中移走
				this.redisTemplate.opsForList().leftPop(key);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String errorStr = errors.toString();
			this.log.error(errorStr.toString());
		}
    	finally {
    		this.releaseCommonLock(lockKey, requestTime);
    	}
    	return retLists;
    }
    
    
    public long  getCommonLock(String lockKey,int lockSeconds)
    {
    	if(lockSeconds>20)
    	{
    		return getCommonLock(lockKey,lockSeconds,lockSeconds-5);
    	}
    	else
    	{
    		return getCommonLock(lockKey,lockSeconds,lockSeconds-2);
    	}
	}
    /**
	 * 申请通用锁，等待30秒
	 * @param lockKey
	 * @return  0--失败
	 */
	public long  getCommonLock(String lockKey,int lockSeconds,int waitSeconds){  
		try{  
            
            long startTime = System.currentTimeMillis();  
            boolean needWait = false;
            while (true){
            	{
	            	if(redisTemplate.opsForValue().setIfAbsent(lockKey,String.valueOf(startTime))){  
	            		startTime = System.currentTimeMillis(); 
	            		redisTemplate.opsForValue().set(lockKey,String.valueOf(startTime),lockSeconds,TimeUnit.SECONDS);  
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
		                    if(System.currentTimeMillis() - requestTimeL > lockSeconds*1000){  
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
                if(System.currentTimeMillis() - startTime > waitSeconds*1000){  
                    return 0;  
                }  
                //延迟一段时间
                Thread.sleep(300);  
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
	
	
	protected String getOrderMainKey(String category,String orderid)
	{
		return "orderMain:"+category+":" + orderid;
	}
	
	protected String getOrderContextKey(String category,String orderid,String key)
	{
		return "orderContext:"+category+":" + orderid + ":" + key;
	}
	
	public String getOrderMainLockKey(String category,String orderid)
	{
		return "orderLock:"+category+":" + orderid;
	}
	
	/**
	 * 将orderMain放入队列
	 * @param orderMain
	 * @return
	 */
	public boolean putOrderMainToCache(OrderMain orderMain)
	{
		try {
			String orderKey = getOrderMainKey(orderMain.getCatetory(),orderMain.getOrderId());
			this.redisTemplate.opsForValue().set(orderKey, orderMain,orderCacheExpireHours,TimeUnit.HOURS);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean putOrderContextToCache(String category,String orderId,String key,String context)
	{
		try {
			if(!StringUtils.isEmpty(context))
			{
				String orderKey = getOrderContextKey(category,orderId,key);
				this.redisTemplate.opsForValue().set(orderKey,context ,orderCacheExpireHours,TimeUnit.HOURS);
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public String getOrderContextFromCache(String category,String orderId,String key)
	{
		try {
			String orderKey = getOrderContextKey(category,orderId,key);
			return (String)this.redisTemplate.opsForValue().get(orderKey);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 
	 */
	public OrderMain getOrderMainFromCache(String category,String orderId)
	{
		String orderKey = getOrderMainKey(category,orderId);
		OrderMain orderMain = (OrderMain)this.redisTemplate.opsForValue().get(orderKey);
		return orderMain;
	}
}
