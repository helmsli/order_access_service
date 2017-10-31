package com.company.orderAccess.serverManager.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderAccess.service.OrderManagerService;
import com.company.orderAccess.service.impl.RedisOrderIDServiceImpl;
import com.company.orderDef.service.OrderDefService;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderFlowDef;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;
@Service("orderManagerService")
public class OrderManagerServiceImpl extends OrderDefService implements OrderManagerService,InitializingBean {
	/**
	 * 定义订单流程定义的数据库
	 */
	@Value("${order.defDbUrl}")  
	private String orderDefDbUrl;
	
	@Resource(name="orderTaskService")
	private OrderTaskServiceImpl orderTaskService;
	
	@Resource(name="dbOrderTaskService")
	private DbOrderTaskService dbOrderTaskService;
	
	@Override
	public ProcessResult createOrder(OrderMainContext orderMainContext) {
		// TODO Auto-generated method stub
		/**
		 * 获取订单定义和步骤定义；
		 */
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		OrderFlowDef orderFlowDef  = this.getOrderDef(orderMainContext.getCatetory(), orderMainContext.getOwnerKey());
		List<OrderFlowStepdef> stepDefLists  = this.getOrderStepDef(orderMainContext.getCatetory(), orderMainContext.getOwnerKey());
		//如果订单信息不全
		if(stepDefLists==null||orderFlowDef==null||stepDefLists.size()==0)
		{
			processResult.setRetCode(OrderAccessConst.RESULT_ERROR_OrderNotExist);
			return processResult;
		}
		orderMainContext.setCurrentStep(orderMainContext.Step_start);
		orderMainContext.setCurrentStatus(orderMainContext.STATUS_initial);
		orderMainContext.setFlowId("0");
		processResult= dbOrderTaskService.modifyOrderMain(orderMainContext);
		//保存到数据库，并且保存到redis中
		return processResult;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		super.setHttpOrderDbDefUrl(orderDefDbUrl);
	}

	@Override
	public ProcessResult startOrder(String category, String orderId) {
		// TODO Auto-generated method stub
		//1.获取orderID，获取order步骤定义
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		
		OrderMain orderMain = this.dbOrderTaskService.getOrderMain(category, orderId);
		//订单步骤不对
		if(orderMain.Step_start.compareToIgnoreCase(orderMain.getCurrentStep())!=0||
				orderMain.STATUS_initial!=orderMain.getCurrentStatus()	)
		{
			processResult.setRetCode(OrderAccessConst.RESULT_ERROR_OrderHavedRunning);
			return processResult;
		}
		//need to get start step
		OrderFlowStepdef orderFlowStepdef  =this.getOrderStepDef(category, orderMain.Step_start, "");
		
		//保存到redis和数据库中
		processResult = orderTaskService.processStartTask(orderMain,orderFlowStepdef);
		
		return processResult;
	}

	

}
