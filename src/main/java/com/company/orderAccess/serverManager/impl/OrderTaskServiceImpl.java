package com.company.orderAccess.serverManager.impl;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
import com.xinwei.orderDb.domain.OrderMain;
@Service("orderTaskService")
public class OrderTaskServiceImpl {
	@Resource(name="dbOrderTaskService")
	private DbOrderTaskService dbOrderTaskService;
	@Resource(name="orderTaskNotifyScheduler")
	private OrderTaskNotifyScheduler orderTaskNotify;
	/**
	 * 处理进入任务的任务调度
	 * @param orderMain
	 * @return
	 */
	public ProcessResult processStartTask(OrderMain orderMain,OrderFlowStepdef orderFlowStepdef)
	{
		//notify
		ProcessResult processResult = this.dbOrderTaskService.jumpToNextStep(orderMain, orderFlowStepdef);
		if(processResult.getRetCode()==OrderAccessConst.RESULT_Success && 
				!StringUtils.isEmpty(orderFlowStepdef.getTaskIn()))
		{
			OrderTaskRunInfo orderTaskInfo=(OrderTaskRunInfo)processResult.getResponseInfo();
			orderTaskNotify.notifyNewTask(orderTaskInfo);
		}
		return processResult;
		
		
	}
	
	
}
