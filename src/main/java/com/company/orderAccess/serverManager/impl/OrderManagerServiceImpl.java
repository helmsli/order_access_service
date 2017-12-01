package com.company.orderAccess.serverManager.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderAccess.service.OrderManagerService;
import com.company.orderDef.service.OrderDefService;
import com.company.orderTask.domain.OrderTaskInDef;
import com.company.userOrder.domain.UserOrder;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderFlow;
import com.xinwei.orderDb.domain.OrderFlowDef;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;
import com.xinwei.orderDb.domain.StepJumpDef;
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
	
	
	@Value("${order.userOrderDbWriteUrl}")
	private String orderUserDbWriteUrl;
	
	
	
	@Autowired
	private  RestTemplate restTemplate = null;
	
	
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
		orderMainContext.setIsFinished(0);
		processResult= dbOrderTaskService.saveOrderMainContext(orderMainContext);
		if(processResult!=null&& processResult.getRetCode()==0)
		{
			if(orderFlowDef.getDeployId().compareToIgnoreCase(orderFlowDef.DeployId_noAutoDeploy)==0)
			{
				
			}
			else if(orderFlowDef.getDeployId().compareToIgnoreCase(orderFlowDef.DeployId_constTime)==0)
			{
				UserOrder userOrder = new UserOrder();
				userOrder.setCategory(orderMainContext.getCatetory());
				userOrder.setOrderId(orderMainContext.getOrderId());
				userOrder.setStatus(userOrder.STATUS_CreateOrder);
				userOrder.setUserId(orderMainContext.getOwnerKey());
				userOrder.setConstCreateTime();
				processResult=saveUserOrderToDb(userOrder);
			}
			else if(orderFlowDef.getDeployId().compareToIgnoreCase(orderFlowDef.DeployId_systemTime)==0)
			{
				UserOrder userOrder = new UserOrder();
				userOrder.setCategory(orderMainContext.getCatetory());
				userOrder.setOrderId(orderMainContext.getOrderId());
				userOrder.setStatus(userOrder.STATUS_CreateOrder);
				userOrder.setUserId(orderMainContext.getOwnerKey());
				processResult=saveUserOrderToDb(userOrder);
			}
		}
		//保存到数据库，并且保存到redis中
		return processResult;
	}

	protected ProcessResult saveUserOrderToDb(UserOrder userOrder)
	{
		ProcessResult result = null;
		try {
			result  = restTemplate.postForObject(orderUserDbWriteUrl + "/" +  userOrder.getCategory()+ "/" + userOrder.getUserId() + "/configUserOrder" ,userOrder ,ProcessResult.class);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = new ProcessResult();
			result.setRetCode(OrderAccessConst.RESULT_Error_Fail);
			if(!StringUtils.isEmpty(e.getMessage()))
			{
				result.setRetMsg(e.getMessage().substring(0, 128));
			}	
		}
		return result;

    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		super.setHttpOrderDbDefUrl(orderDefDbUrl);
	}

	@Override
	public ProcessResult startOrder(String category, String orderId,JsonRequest jsonRequest) {
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
		OrderTaskInDef orderTaskInDef=null;
		if(!StringUtils.isEmpty(orderFlowStepdef.getRunInfo()))
		{
		   orderTaskInDef = JsonUtil.fromJson(orderFlowStepdef.getTaskIn(),OrderTaskInDef.class);
		}
		else
		{
			orderTaskInDef = new OrderTaskInDef();
			orderTaskInDef.setCategory(OrderTaskInDef.catogory_manual);
		}
		//保存到redis和数据库中
		processResult = orderTaskService.processStartTask(orderMain,orderFlowStepdef,orderTaskInDef);
		
		return processResult;
	}

	@Override
	public ProcessResult mjumpToNext(String category, OrderFlow orderFlow) {
		// TODO Auto-generated method stub
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		
		OrderMain orderMain = this.dbOrderTaskService.getOrderMain(category, orderFlow.getOrderId());
		if(orderMain==null)
		{
			processResult.setRetCode(OrderAccessConst.RESULT_ERROR_OrderNotExist);
			return processResult;
		}
		if(orderMain.getFlowId().compareTo(orderFlow.getFlowId())!=0 ||
				orderMain.getCurrentStep().compareToIgnoreCase(orderFlow.getStepId())!=0||
				orderMain.getCurrentStatus()!=orderFlow.getCurrentStatus())
		{
			//状态不正确；
			processResult.setRetCode(OrderAccessConst.RESULT_ERROR_NoFlowID);
			return processResult;
		}
		
		OrderFlowStepdef orderFlowStepdef  =this.getOrderStepDef(category, orderFlow.getStepId(), "");
		
		StepJumpDef stepJumpDef = orderFlowStepdef.getStepJumpDef(Integer.parseInt(orderFlow.getRetCode()));
		OrderFlowStepdef nextOrderFlowStepdef= getOrderStepDef(category, stepJumpDef.getNextStep(), ""); 
		//根据下一步确定运行信息
		OrderTaskInDef orderTaskInDef=null;
		if(!StringUtils.isEmpty(nextOrderFlowStepdef.getRunInfo()))
		{
		   orderTaskInDef = JsonUtil.fromJson(nextOrderFlowStepdef.getTaskIn(),OrderTaskInDef.class);
		}
		else
		{
			orderTaskInDef = new OrderTaskInDef();
			orderTaskInDef.setCategory(OrderTaskInDef.catogory_manual);
		}
		processResult = orderTaskService.processStartTask(orderMain,orderFlowStepdef,orderTaskInDef);
		return processResult;
	}

	

}
