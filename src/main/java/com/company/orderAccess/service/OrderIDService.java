package com.company.orderAccess.service;

import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;

public interface OrderIDService {
	/**
	 * 创建订单ID
	 * @param catetory
	 * @param ownerKey
	 * @param jsonRequest
	 * @return
	 */
	public ProcessResult createOrderId(String category,String ownerKey,JsonRequest jsonRequest);
}
