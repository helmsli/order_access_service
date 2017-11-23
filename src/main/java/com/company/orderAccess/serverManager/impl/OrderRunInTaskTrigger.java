package com.company.orderAccess.serverManager.impl;

import org.springframework.web.client.RestTemplate;

import com.company.orderTask.domain.OrderTaskInDef;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderMain;

public class OrderRunInTaskTrigger implements Runnable {
	private OrderTaskRunInfo orderTaskInfo;
	private  RestTemplate notifyTemplate = null;
	private OrderTaskInDef orderTaskInDef;
	public OrderRunInTaskTrigger(OrderTaskInDef orderTaskInDef,OrderTaskRunInfo orderTaskInfo,RestTemplate notifyTemplate)
	{
		this.orderTaskInfo = orderTaskInfo;
		this.notifyTemplate = notifyTemplate;
		this.orderTaskInDef=orderTaskInDef;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ProcessResult result = null;
		try {
			result  = notifyTemplate.postForObject(this.orderTaskInDef.getTriggerUrl() + "/" + orderTaskInfo.getCatetory() + "/" +  OrderMain.getDbId(orderTaskInfo.getOrderId()) + "/" +orderTaskInfo.getOrderId() + "/" +orderTaskInfo.getCurrentStep()+ "/" + orderTaskInDef.getTriggerRestMethod()  ,orderTaskInfo, ProcessResult.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			throw e;
			
		}

	}

}
