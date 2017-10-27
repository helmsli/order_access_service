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
	@Resource(name="redisOrderTaskService")
	private RedisOrderTaskService redisOrderTaskService;
	@Resource(name="orderTaskNotify")
	private OrderTaskNotify orderTaskNotify;
	/**
	 * 处理进入任务的任务调度
	 * @param orderMain
	 * @return
	 */
	public ProcessResult processInTask(OrderMain orderMain)
	{
		/**
		 * 根据订单当前状态获取订单步骤。
		 */
		ProcessResult ProcessResult = new ProcessResult();
		ProcessResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		OrderFlowStepdef OrderFlowStepdef=null;
		//如果没有找到步骤信息
		if(OrderFlowStepdef==null)
		{
			ProcessResult.setRetCode(OrderAccessConst.RESULT_ERROR_noStepInfo);
			return ProcessResult;
		}
		//没有进入的任务信息
		if(StringUtils.isEmpty(OrderFlowStepdef.getTaskIn()))
		{
			ProcessResult.setRetCode(OrderAccessConst.RESULT_ERROR_noRunningTask);
			return ProcessResult;
		}
		//将任务信息放入信息调度队列
		OrderTaskRunInfo orderTaskInfo =new OrderTaskRunInfo();
		orderTaskInfo.setCatetory(orderMain.getCatetory());
		orderTaskInfo.setOrderId(orderMain.getOrderId());
		orderTaskInfo.setCurrentStatus(orderMain.getCurrentStatus());
		orderTaskInfo.setCurrentStep(orderMain.getCurrentStep());
		orderTaskInfo.setFlowId(orderMain.getFlowId());
		redisOrderTaskService.putOrderTaskToQuene(orderTaskInfo);
		//notify
		orderTaskNotify.notifyNewTask(orderTaskInfo);
		
		return ProcessResult;
		
		
	}
	
	
}
