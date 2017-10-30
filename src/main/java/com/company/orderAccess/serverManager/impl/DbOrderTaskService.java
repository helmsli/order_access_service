package com.company.orderAccess.serverManager.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderMain;

@Service("dbOrderTaskService")
public class DbOrderTaskService {
	@Value("order.orderDbServiceUrl")
	private String httpOrderDbUrl;
	
	
	public  ProcessResult getOrderMain(String category,String orderId)
	{
			return null;
	}
	
	/**
	 * 
	 */
	public ProcessResult modifyOrderMain(String category,String orderId)
	{
		return null;
	}
	/**
	 * 保存主订单到数据库
	 * @param orderMain
	 * @return
	 */
	protected boolean saveOrderMainToDb(OrderMain orderMain,String ownerkey)
	{
			return true;
	}
	
	/**
	 * 获取主订单从数据库
	 */
	protected OrderMain getOrderMainFromDb(String category,String orderId,String ownerkey)
	{
		return null;
	}
}
