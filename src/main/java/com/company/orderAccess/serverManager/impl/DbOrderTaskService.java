package com.company.orderAccess.serverManager.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.Const.OrderDbConst;
import com.xinwei.orderDb.domain.OrderFlow;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;
import com.xinwei.orderDb.domain.StepJumpingRequest;
import com.xinwei.userOrders.domain.UserOrders;

@Service("dbOrderTaskService")
public class DbOrderTaskService {

	@Value("${order.defDbUrl}")  
	private String orderDefDbUrl;
	
	@Value("order.orderDbServiceUrl")
	private String httpOrderDbUrl;

	@Value("order.userOrdersServiceUrl")
	private String httpUserOrdersUrl;
	
	@Resource(name="redisOrderTaskService")
	private RedisOrderTaskService redisOrderTaskService;
	
	private RestTemplate restTemplate;

	/**
	 * 设置步骤跳转信息，如果成功，返回信息；
	 * @param orderMain
	 * @param orderFlowStepdef -- 如果是第一步，步骤id填写start
	 * @return
	 */
	public ProcessResult jumpToNextStep(OrderMain orderMain,OrderFlowStepdef orderFlowStepdef)
	{
		
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		
		//如果没有找到步骤信息
		if(orderFlowStepdef==null)
		{
			processResult.setRetCode(OrderAccessConst.RESULT_ERROR_noStepInfo);
			return processResult;
		}
		//判断是否有进入步骤的运行信息
		boolean haveRunInTask = !(StringUtils.isEmpty(orderFlowStepdef.getTaskIn())); 
		//定义步骤id
		int newFlowId = Integer.parseInt(orderMain.getFlowId());
		newFlowId++;
		//构造新的步骤信息
		OrderFlow nextOrderFlow = new OrderFlow();
		nextOrderFlow.setOrderId(orderMain.getOrderId());
		nextOrderFlow.setCurrentStatus(nextOrderFlow.STATUS_initial);
		nextOrderFlow.setFlowId(String.valueOf(newFlowId));
		nextOrderFlow.setStepId(orderFlowStepdef.getStepId());
		//构造老的步骤信息
		OrderFlow preOrderFlow = new OrderFlow();
		nextOrderFlow.setOrderId(orderMain.getOrderId());
		nextOrderFlow.setCurrentStatus(nextOrderFlow.STATUS_ending);
		nextOrderFlow.setFlowId(orderMain.getFlowId());
		nextOrderFlow.setStepId(orderMain.getCurrentStep());
		
		//是否删除上一步需要自动运行的
		int needDeletePreStepRunning = 1;
		//是否最新的一步需要自动运行
		int needAddRunningNowStep = 1;
		if(!haveRunInTask)
		{
			needAddRunningNowStep=0;
		}
		else
		{
			nextOrderFlow.setCurrentStatus(nextOrderFlow.STATUS_running);
		}
		//将总流程的步骤信息设置为新的步骤信息
		orderMain.setCurrentStep(nextOrderFlow.getStepId());
		orderMain.setCurrentStatus(nextOrderFlow.getCurrentStatus());
		orderMain.setFlowId(nextOrderFlow.getFlowId());
		
		//更新数据库
		processResult = this.updateOrderFlowStepJumping(nextOrderFlow, preOrderFlow, needAddRunningNowStep, needDeletePreStepRunning);
		
		if(processResult.getRetCode()==OrderAccessConst.RESULT_Success&&haveRunInTask)
		{
			//将任务信息放入信息调度队列
			OrderTaskRunInfo orderTaskInfo =new OrderTaskRunInfo();
			orderTaskInfo.setCatetory(orderMain.getCatetory());
			orderTaskInfo.setOrderId(orderMain.getOrderId());
			orderTaskInfo.setCurrentStatus(orderMain.getCurrentStatus());
			orderTaskInfo.setCurrentStep(orderMain.getCurrentStep());
			orderTaskInfo.setFlowId(orderMain.getFlowId());
			redisOrderTaskService.putOrderTaskToQuene(orderTaskInfo);
			//notify
			processResult.setResponseInfo(orderTaskInfo);
			
		}
		return processResult;
	}
	
	
	public OrderMain getOrderMain(String category, String orderId) {
		
		
		OrderMain orderMain = redisOrderTaskService.getOrderMainFromCache(category, orderId);
		if(orderMain==null)
		{
			orderMain = selectOrderMainFromDb(category,orderId);
			if(orderMain!=null)
			{
				redisOrderTaskService.putOrderMainToCache(orderMain);
			}
		}
		return orderMain;
	}

	public ProcessResult saveOrderMainContext(String category, String orderId,Map<String,String>maps)
	{
		return null;
	}
	
	
	public Map<String,String> getOrderMainContext(String category, String orderId,List<String>keys)
	{
		Map<String,String> maps = new HashMap<String,String>();
		boolean isNeedQueryDb = false;
		//从redis中获取
		for(int i=0;i<keys.size();i++)
		{
			String value = this.redisOrderTaskService.getOrderContextFromCache(category, orderId, keys.get(i));
			if(StringUtils.isEmpty(value))
			{
				isNeedQueryDb = true;
				break;
			}
			else
			{
				maps.put(keys.get(i),value);
			}
		}
		if(isNeedQueryDb)
		{
			//从数据库查询
			maps  =this.selectOrderContextDataFromDb(category,orderId, keys);
			try {
				//更新redis
				for (Map.Entry<String,String> entry : maps.entrySet()) {  
					redisOrderTaskService.putOrderContextToCache(category, orderId, entry.getKey(), entry.getValue());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		return maps;
	}
	/**
	 * 跟距orderId更新orderMain
	 */
	public ProcessResult modifyOrderMain(OrderMain orderMain) {
		// TODO Auto-generated method stub
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderDbConst.RESULT_Error_DbError);
		try {
			this.redisOrderTaskService.putOrderMainToCache(orderMain);
			processResult = modifyOrderMainToDb(orderMain.getCatetory(),orderMain.getOrderId(),orderMain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return processResult;
		
	}

	
	
	protected ProcessResult modifyOrderMainToDb(String category, String orderId, OrderMain orderMain) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/updateMainOrder",
				orderMain, ProcessResult.class);
		return processResult;
	}
	/**
	 * 插入保存orderMain
	 * 
	 * @param orderMain
	 * @return
	 */
	protected ProcessResult saveOrderMainToDb(OrderMainContext orderMainContext) {

		ProcessResult processResult = new ProcessResult();
		String orderId = orderMainContext.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/addOrderMain",
				orderMainContext, ProcessResult.class);
		if (processResult.getRetCode() == 0) {
			return processResult;
		}
		return null;
	}

	/**
	 * 跟距orderId查詢orderMain
	 */
	protected OrderMain selectOrderMainFromDb(String category, String orderId) {

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/getOrderMainFromDb",
				null, ProcessResult.class);
		if (processResult.getRetCode() == 0) {
			OrderMain orderMain = (OrderMain) processResult.getResponseInfo();
			return orderMain;
		}
		return null;
	}

	/**
	 * 更新订单状态，用于将订单从步骤A跳转到步骤B
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{dbId}/{orderId}/stepjumping
	 * @param nextorderFlow
	 * @param preOrderFlow
	 * @param nextOrderAutoRun
	 *            指明是否需要后台自动运行 nextOrderFlow
	 * @param preOrderAutoRun
	 *            第一个 orderflow为老的流程信息，第二个orderflow为新的流程信息。 第一个订单必须填写状态，步骤，流程id，
	 * @return
	 */
	public ProcessResult updateOrderFlowStepJumping(OrderFlow nextOrderFlow, OrderFlow preOrderFlow,
			int nextOrderAutoRun, int preOrderAutoRun) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = nextOrderFlow.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		StepJumpingRequest stepJumpingRequest = new StepJumpingRequest();
		stepJumpingRequest.setNextOrderAutoRun(nextOrderAutoRun);
		stepJumpingRequest.setNextOrderFlow(nextOrderFlow);
		stepJumpingRequest.setPreOrderAutoRun(preOrderAutoRun);
		stepJumpingRequest.setPreOrderFlow(preOrderFlow);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/stepjumping",
				stepJumpingRequest, ProcessResult.class);
		return processResult;
	}

	/**
	 * url:http://127.0.0.1:8088/orderDb/{dbId}/{orderId}/configOrderFlow
	 * 用于执行步骤过程中，步骤没有发生跳转，仅仅更新步骤运行的结果
	 * 
	 * @param url
	 * @param orderFlow
	 * @return
	 */
	public ProcessResult updateOrderFlowStatus(OrderFlow orderFlow) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = orderFlow.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/configOrderFlow",
				orderFlow, ProcessResult.class);
		return processResult;
	}

	/**
	 * 用于挂起，重新启动，同步主订单更新时间
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{dbId}/{orderId}/updateStepStatus
	 * @param orderFlow
	 * @return
	 */
	public ProcessResult suspendOrRestartOrderFlow(OrderFlow orderFlow) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = orderFlow.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/updateStepStatus",
				orderFlow, ProcessResult.class);

		return processResult;
	}

	/**
	 * 获取订单上下文信息
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{dbId}/{orderId}/getContextData
	 * @param contextKeys
	 * @return
	 */
	public Map<String,String> selectOrderContextDataFromDb(String category,String orderId,List<String> keys) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		JsonRequest jsonRequest = new JsonRequest();
		jsonRequest.setJsonString(JsonUtil.toJson(keys));
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + category + "/" +dbId + "/" + orderId + "/getContextData",
				jsonRequest, ProcessResult.class);
		if(processResult.getRetCode()==OrderAccessConst.RESULT_Success)
		{
			Map<String,String> contextMaps = JsonUtil.fromJson((String)processResult.getResponseInfo());
			return contextMaps;
		}
		return null;
	}

	/**
	 * 新增订单上下文信息
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{dbId}/{orderId}/putContextData
	 * @param contextDatas
	 * @return
	 */
	public ProcessResult putOrderContextData(OrderMainContext orderMainContext) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = orderMainContext.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/putContextData",
				orderMainContext, ProcessResult.class);
		return processResult;
	}

	/**
	 * 根据分类查询单条orderDef
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{category}/{ownerKey}/getOrderDef
	 * @return
	 */
	public ProcessResult selectOrderDefByCategory(String category, String ownerKey) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		processResult = restTemplate.getForObject(orderDefDbUrl + "/" + category + "/" + ownerKey + "/getOrderDef",
				null, ProcessResult.class);
		return processResult;
	}

	/**
	 * 根据分类查询所有的orderStepdef
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{category}/{ownerKey}/getOrderStepDef
	 * @return
	 */
	public ProcessResult selectOrderStepDefsByCategory(String category, String ownerKey) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		processResult = restTemplate.getForObject(orderDefDbUrl + "/" + category + "/" + ownerKey + "/getOrderStepDef",
				null, ProcessResult.class);
		return processResult;
	}

	/**
	 * @param jsonRequest
	 *            {"stepId":"xxxxxxxxxxxxxxx","flowId":"xxxxxxxxxxx"}
	 * @param orderId
	 * @return
	 */
	public ProcessResult selectOrderFlow(JsonRequest jsonRequest, String orderId) {
		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + dbId + "/" + orderId + "/selectOrderFlow",
				jsonRequest, ProcessResult.class);
		return processResult;
	}

	/**
	 * 根据orderId查询userOrders ownerKey createTime orderId三属性确定一条记录
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/selectUserOrdersById
	 * @param userOrders
	 * @return
	 */
	public ProcessResult selectUserOrdersByOrderId(UserOrders userOrders) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = userOrders.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(
				httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/selectUserOrdersById", userOrders,
				ProcessResult.class);
		return processResult;
	}

	/**
	 * 根据状态查询userOrders ownerKey createTime currentStatus三属性确定一条记录
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/selectUserOrdersByStatus
	 * @param userOrders
	 * @return
	 */
	public ProcessResult selectUserOrdersByOrderStatus(UserOrders userOrders) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = userOrders.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(
				httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/selectUserOrdersByStatus", userOrders,
				ProcessResult.class);
		return processResult;
	}

	
	
	/**
	 * 根据orderId更新userOrders的状态
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/updateUserOrdersStatus
	 * @param userOrders
	 * @return
	 */
	public ProcessResult updateUserOrdersStatus(UserOrders userOrders) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = userOrders.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(
				httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/updateUserOrdersStatus", userOrders,
				ProcessResult.class);
		return processResult;
	}

	/**
	 * 插入一条userOrders
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/insertUserOrders
	 * @param userOrders
	 * @return
	 */
	public ProcessResult insertUserOrders(UserOrders userOrders) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = userOrders.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/insertUserOrders",
				userOrders, ProcessResult.class);
		return processResult;
	}

	/**
	 * 删除一条userOrders
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/deleteUserOrders
	 * @return
	 */
	public ProcessResult deleteUserOrders(String orderId) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/deleteUserOrders",
				null, ProcessResult.class);
		return processResult;
	}
}