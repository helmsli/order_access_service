package com.company.orderAccess.service;

import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderFlow;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;

public interface OrderManagerService {
	/**
	 * 创建一个新的订单
	 * 
	 * @param orderMainContext
	 * @return
	 */
	public ProcessResult createOrder(OrderMainContext orderMainContext);

	/**
	 * 启动一个初始化后的订单
	 * 
	 * @param category
	 * @param orderId
	 * @return
	 */
	public ProcessResult startOrder(String category, String orderId);

	
	public ProcessResult mjumpToNext(String category,OrderFlow orderFlow);

}
