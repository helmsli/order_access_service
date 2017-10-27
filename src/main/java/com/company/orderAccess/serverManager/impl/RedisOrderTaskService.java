package com.company.orderAccess.serverManager.impl;

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
	
	/**
	 * 创建
	 * @param queneName
	 * @return
	 */
	public String getOrderTaskQueneKey(String queneName,String category)
	{
		return Prefix_key_OrderTaskQuene + ":" + category  + ":" +queneName;
	}
	/**
	 * 将订单任务放入队列
	 * @param orderTaskInfo
	 * @return
	 */
    public boolean putOrderTaskToQuene(OrderTaskRunInfo orderTaskInfo)
    {
    	String key = this.getOrderTaskQueneKey(orderTaskQueneName,orderTaskInfo.getCatetory());
    	this.redisTemplate.opsForList().rightPush(key, orderTaskInfo);
		return true;
    }
}
