package com.company.orderAccess.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderAccess.domain.OrderIDDef;
import com.company.orderAccess.domain.OrderIdCache;
import com.google.gson.reflect.TypeToken;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderFlowDef;
import com.xinwei.orderDb.domain.OrderFlowStepdef;

@Service("redisOrderIdService")
public class RedisOrderIDServiceImpl {
	
	//default:24*3600000
	@Value("${order.orderIdDef.expireMillis:86400000}")
	private long  MillisPerDay;
	
	@Value("${order.orderIdDef.startOrderEveryDay:86400}")
	private long  amountEveryDay;

	private Logger log = LoggerFactory.getLogger(getClass());
	
	
	private Map<String,ProcessResult> orderDefList = new ConcurrentHashMap<String,ProcessResult>();
	
	
	/**
	 * 订单ID定义的key
	 */
	private String Prefix_key_OrderIdDef = "orderIdDef:1.0";
	
	
	/**
	 * orderid的生成规则
	 */
	private String Prefix_key_OrderIdRule = "orderIdrule:1.0";
	
	/**
	 * 已经使用的orderId的cache
	 */
	private String Prefix_key_OrderIdUsed = "orderIdUsed:1.0";
	
	private String Prefix_key_OrderDbUsed = "orderDbUsed:1.0";
	
	@Resource(name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	
	
	
	/**
	 * 判断cache是否过期
	 * @return
	 */
	protected boolean isCacheExpired(ProcessResult processResult)
	{
		int createDays =processResult.getRetCode();
		int nowDay = (int)(System.currentTimeMillis()/(MillisPerDay));
		if(nowDay-createDays>1)
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * 订单id已经使用的cache
	 * @param category
	 * @return
	 */
	protected String getOrderIdUsedKey(String category,String orderDefId)
	{
		return Prefix_key_OrderIdUsed + ":" + category + ":" + orderDefId;
	}
	
	protected String getOrderIdLockUsedKey(String category,String orderDefId)
	{
		return  "_lock_:" + category + ":" + orderDefId;
	}
	/**
	 * 使用的数据库DB和orderId的key
	 * @param category
	 * @param orderDefId
	 * @return
	 */
	protected String getOrderDbUsedKey(String category,String orderDefId)
	{
		
		return Prefix_key_OrderDbUsed + ":" + category + ":" + orderDefId;
	}
	
	/**
	 * 对应的category的生成规则
	 * @param category
	 * @return
	 */
	protected String getOrderIdRuleKey(String category)
	{
		return Prefix_key_OrderIdRule + ":" + category;
	}
	
	
	
	
	/**
	 * 用于生成订单ID
	 * @param category
	 * @return
	 */
	protected String getOrderIdDefKey(String category)
	{
		return Prefix_key_OrderIdDef + ":" + category;
	}
	
	public int putOrderIdRuleToCache(String category,OrderIDDef orderIDDef)
	{
		String key = getOrderIdRuleKey(category);
		redisTemplate.opsForValue().set(key, orderIDDef);
		return 0;
		
	}
	
	
	
	public int putOrderIdDefToCache(String category,OrderFlowDef orderFlowDef)
	{
		try {
			
			String key = getOrderIdDefKey(category);
			//放入本地缓存
			ProcessResult processResult = new ProcessResult();
			processResult.setRetCode((int)(System.currentTimeMillis()/(this.MillisPerDay)));
			processResult.setResponseInfo(orderFlowDef);
			orderDefList.put(key, processResult);
			//放入redis，不能放入redis，因为需要在web侧进行路由，不同的ownerkey路由给不同的应用服务器，以支持不同的orderIDDef；
			//redisTemplate.opsForValue().set(key, orderFlowDef,7,TimeUnit.DAYS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return OrderAccessConst.RESULT_Success;
	}
	/**
	 * 订单ID定义的对象；
	 * @param category
	 * @return
	 */
	public OrderFlowDef getOrderIdDefFromCache(String category)
	{
		ProcessResult processResult =null;
		try {
			String key = getOrderIdDefKey(category);
			OrderFlowDef orderFlowDef=null;
			if(orderDefList.containsKey(key))
			{
				
				processResult=this.orderDefList.get(key);
				if(!this.isCacheExpired(processResult))
				{
					orderFlowDef =(OrderFlowDef)processResult.getResponseInfo(); 
					return orderFlowDef;
				}
			}
			//orderFlowDef  =(OrderFlowDef)redisTemplate.opsForValue().get(key);
			return orderFlowDef;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
		
	
	/**
	 * 从redis中创建orderid的起始编号
	 * @param category
	 * @param numbers
	 * @return
	 */
	public long createOrderIdFromRedis(String category,String orderIdDef,int numbers) {
		// TODO Auto-generated method stub
		String key = this.getOrderIdUsedKey(category,orderIdDef);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		Long retValue= opsForValue.increment(key, numbers);
		log.debug("ret value:" + retValue.toString() + ":" +numbers );
		//如果原来没有数值；
		if(retValue.longValue()<numbers*20)
		{
			String lockKey = getOrderIdLockUsedKey(category,orderIdDef);
			return setDefaultOrderId(lockKey,key,numbers);
			
		}
		return retValue.longValue();
	}
	/**
	 * 如果默认的数值小于数字，则创建一个默认的数值
	 * @param lockKey
	 * @param key
	 * @param numbers
	 * @return
	 */
	protected long setDefaultOrderId(String lockKey,String key,int numbers)
	{
		long requestTime = 0;
		try {
			requestTime = this.getCommonLock(lockKey, 30);
			ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
			Long retValue=new Long(0);
			try {
				int oldValue = (Integer) opsForValue.get(key);
				retValue = new Long(oldValue);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.debug("new value:" + retValue);
			if(retValue>numbers*20)
			{
				retValue= opsForValue.increment(key, numbers);
				return 	retValue.longValue();
			}
			
			Calendar now = Calendar.getInstance();
			Calendar oldDate = Calendar.getInstance();
			oldDate.set(2017, 10, 30, 0,0);
			int days = (int)((now.getTime().getTime() - oldDate.getTime().getTime())/(1000*3600*24));
			//Long newValue = new Long(days*this.amountEveryDay);
			//opsForValue.set(key, newValue);
			retValue= opsForValue.increment(key, days*this.amountEveryDay);
			log.debug("set new value:" + days*this.amountEveryDay);
			retValue= opsForValue.increment(key, numbers);
			return 	retValue.longValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			this.releaseCommonLock(lockKey, requestTime);
		}
		return 0;
	
	
	}
	
	/**
	 * 从redis中获取分区的定义
	 * @param category
	 * @param orderIdDef
	 * @return
	 */
	protected OrderIDDef getDbIdFromRedis(String category,String orderIdDef) {
		// TODO Auto-generated method stub
		String key = this.getOrderDbUsedKey(category,orderIdDef);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		OrderIDDef orderIDDef = (OrderIDDef)opsForValue.get(key);
		return orderIDDef;
	}
	/**
	 * 将分区的定义放入到redis中
	 * @param category
	 * @param orderIdDef
	 * @param orderIDDef
	 */
	protected void putDbIdToRedis(String category,String orderIdDef,OrderIDDef orderIDDef) {
		// TODO Auto-generated method stub
		String key = this.getOrderDbUsedKey(category,orderIdDef);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		opsForValue.set(key, orderIDDef);
		return ;
	}
	public long  getCommonLock(String lockKey,int lockSeconds){  
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
                if(System.currentTimeMillis() - startTime > lockSeconds*1000){  
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
	

}
