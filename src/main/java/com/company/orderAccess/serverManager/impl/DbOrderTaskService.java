package com.company.orderAccess.serverManager.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderTask.domain.OrderTaskInDef;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.google.gson.reflect.TypeToken;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.Const.OrderDbConst;
import com.xinwei.orderDb.domain.OrderFlow;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;
import com.xinwei.orderDb.domain.StepJumpingRequest;

@Service("dbOrderTaskService")
public class DbOrderTaskService {

	@Value("${order.defDbUrl}")  
	private String orderDefDbUrl;
	
	@Value("${order.orderDbServiceUrl}")
	private String httpOrderDbUrl;

	@Value("order.userOrdersServiceUrl")
	private String httpUserOrdersUrl;
	
	@Resource(name="redisOrderTaskService")
	private RedisOrderTaskService redisOrderTaskService;
	
	@Autowired
	private RestTemplate restTemplate;

	 private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 设置步骤跳转信息，如果成功，返回信息；
	 * @param orderMain
	 * @param orderFlowStepdef -- 如果是第一步，步骤id填写start
	 * @return
	 */
	public ProcessResult jumpToNextStep(OrderMain orderMain,OrderFlowStepdef orderFlowStepdef,OrderTaskInDef orderTaskInDef,ProcessResult runResult)
	{
		
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		
		//如果没有找到步骤信息
		if(orderFlowStepdef==null)
		{
			processResult.setRetCode(OrderAccessConst.RESULT_ERROR_noStepInfo);
			return processResult;
		}
		//判断是否有进入步骤的运行信息,运行信息不为空，并且不是结束任务
		boolean haveRunInTask = (!(StringUtils.isEmpty(orderFlowStepdef.getTaskIn())))&& (orderFlowStepdef.getStepId().compareToIgnoreCase(OrderMain.Step_end)!=0); 
		//定义步骤id
		int newFlowId = Integer.parseInt(orderMain.getFlowId());
		newFlowId++;
		//构造新的步骤信息
		OrderFlow nextOrderFlow = new OrderFlow();
		nextOrderFlow.setCatetory(orderMain.getCatetory());
		nextOrderFlow.setOrderId(orderMain.getOrderId());
		nextOrderFlow.setCurrentStatus(nextOrderFlow.STATUS_initial);
		nextOrderFlow.setFlowId(String.valueOf(newFlowId));
		nextOrderFlow.setStepId(orderFlowStepdef.getStepId());
		nextOrderFlow.setRetryTimes("0");
		//构造老的步骤信息
		OrderFlow preOrderFlow = new OrderFlow();
		preOrderFlow.setCatetory(orderMain.getCatetory());
		preOrderFlow.setOrderId(orderMain.getOrderId());
		preOrderFlow.setCurrentStatus(nextOrderFlow.STATUS_ending);
		preOrderFlow.setFlowId(orderMain.getFlowId());
		preOrderFlow.setStepId(orderMain.getCurrentStep());
		if(runResult!=null)
		{
			try {
				preOrderFlow.setRetCode(String.valueOf(runResult.getRetCode()));
				if(!StringUtils.isEmpty(runResult.getRetMsg()))
				{
					String str = runResult.getRetMsg();
					if(str.length()>128)
					{
					preOrderFlow.setRetMsg(str.substring(0, 128));
					}
					else
					{
						preOrderFlow.setRetMsg(str);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				String errorStr = errors.toString();
				this.logger.error(errorStr.toString());
			}
		}
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
		
		if(processResult.getRetCode()==OrderAccessConst.RESULT_Success)
		{
			//将任务信息放入信息调度队列
			OrderTaskRunInfo orderTaskInfo =new OrderTaskRunInfo();
			orderTaskInfo.setCatetory(orderMain.getCatetory());
			orderTaskInfo.setOrderId(orderMain.getOrderId());
			orderTaskInfo.setCurrentStatus(orderMain.getCurrentStatus());
			orderTaskInfo.setCurrentStep(orderMain.getCurrentStep());
			orderTaskInfo.setFlowId(orderMain.getFlowId());
			orderTaskInfo.setRunTime(System.currentTimeMillis());
			orderTaskInfo.setRuntimes(0);
			if(StringUtils.isEmpty(orderFlowStepdef.getRunInfo()))
			{
				//永不过期
				orderTaskInfo.setExpireTime(0);
			}
			else
			{
				//long expireTimeOut = orderFlowStepdef.getRetryTimes();
				try {
					if(StringUtils.isEmpty(orderFlowStepdef.getRunInfo().trim()))
					{
					long expireTimeOut = Long.parseLong(orderFlowStepdef.getRunInfo());
					orderTaskInfo.setExpireTime(System.currentTimeMillis() + expireTimeOut);
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			 
			if(haveRunInTask&& orderTaskInDef.getCategory()!=OrderTaskInDef.catogory_manual)
			{
				logger.debug(orderTaskInfo.toString());
				redisOrderTaskService.putOrderTaskToQuene(orderTaskInfo);
				
			}
			else
			{
				logger.error("not put to task quene:" + orderTaskInfo.toString());
			}
			processResult=modifyOrderMain(orderMain);
			//notify
			processResult.setResponseInfo(orderTaskInfo);
			
		}
		return processResult;
	}
	
	
	public OrderMain getOrderMain(String category, String orderId) {

		OrderMain orderMain = redisOrderTaskService.getOrderMainFromCache(category, orderId);
		if (orderMain == null) {
			orderMain = selectOrderMainFromDb(category, orderId);
			if (orderMain != null) {
				redisOrderTaskService.putOrderMainToCache(orderMain);
			}
		}
		return orderMain;
	}

	public ProcessResult saveOrderMainContext(String category, String orderId, Map<String, String> maps) {
		return null;
	}
	
	
	public ProcessResult getOrderMainContext(String category, String orderId, List<String> keys) {
		ProcessResult processResult = new ProcessResult();
		Map<String, String> maps = new HashMap<String, String>();
		boolean isNeedQueryDb = false;
		// 从redis中获取
		List<String>querList = new ArrayList<String>();
		for (int i = 0; i < keys.size(); i++) {
			String value = this.redisOrderTaskService.getOrderContextFromCache(category, orderId, keys.get(i));
			if (StringUtils.isEmpty(value)) {
				isNeedQueryDb = true;
				querList.add(keys.get(i));
			} else {
				maps.put(keys.get(i), value);
			}
		}
		processResult.setRetCode(OrderAccessConst.RESULT_Success);
		processResult.setResponseInfo(maps);
		if (isNeedQueryDb) {
			// 从数据库查询
			ProcessResult ret = this.selectOrderContextDataFromDb(category, orderId, querList);
			try {
				if(ret.getRetCode()==0)
				{
					
					Map<String,String>queryMaps =(Map<String, String>) ret.getResponseInfo();
					// 更新redis
					for (Map.Entry<String, String> entry : queryMaps.entrySet()) {
						redisOrderTaskService.putOrderContextToCacheIfAbsent(category, orderId, entry.getKey(), entry.getValue());
						maps.put(entry.getKey(), entry.getValue());
					}
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("",e);
				
			}
		}
		
		return processResult;
	}

	public ProcessResult putOrderMainContext(OrderMainContext orderMainContext) {
		ProcessResult processResult = new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Success);
		{
			// 从数据库查询
			Map<String, String> context = orderMainContext.getContextDatas();
			try {
				// 更新redis
				for (Map.Entry<String, String> entry : context.entrySet()) {
					redisOrderTaskService.putOrderContextToCache(orderMainContext.getCatetory(),
							orderMainContext.getOrderId(), entry.getKey(), entry.getValue());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			processResult = this.putOrderContextDataToDb(orderMainContext);
		}

		return processResult;
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
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + category + "/" + dbId + "/" + orderId + "/updateMainOrder",
				orderMain, ProcessResult.class);
		return processResult;
	}
	/**
	 * 保存订单和上下文数据到数据库
	 * @param orderMainContext
	 * @return
	 */
	public ProcessResult saveOrderMainContext(OrderMainContext orderMainContext) {

		//保存数据到内存
		Map<String, String>  contextMaps = orderMainContext.getContextDatas();
		if(contextMaps!=null)
		{
			for (Map.Entry<String, String> entry : contextMaps.entrySet()) {
				redisOrderTaskService.putOrderContextToCache(orderMainContext.getCatetory(), orderMainContext.getOrderId(), entry.getKey(), entry.getValue());
			}
		}		
		//保存数据到数据库
		return saveOrderMainContextToDb(orderMainContext);
	}
	
	/**
	 * 保存订单和上下文到数据库
	 * 
	 * @param orderMain
	 * @return
	 */
	protected ProcessResult saveOrderMainContextToDb(OrderMainContext orderMainContext) {

		ProcessResult processResult = new ProcessResult();
		String orderId = orderMainContext.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + orderMainContext.getCatetory() + "/" + dbId + "/" + orderId + "/addOrderMain",
				orderMainContext, ProcessResult.class);
		if (processResult.getRetCode() == 0) {
			return processResult;
		}
		return processResult;
	}
	/**
	 * 保存订单信息到数据库(不包括上下文信息)
	 * @param orderMain
	 * @return
	 */
	protected ProcessResult saveOrderMainToDb(OrderMain orderMain) {

		ProcessResult processResult = new ProcessResult();
		String orderId = orderMain.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + orderMain.getCatetory() + "/" + dbId + "/" + orderId + "/addOrderMain",
				orderMain, ProcessResult.class);
		if (processResult.getRetCode() == 0) {
			return processResult;
		}
		return processResult;
	}

	/**
	 * 跟距orderId查詢orderMain
	 */
	protected OrderMain selectOrderMainFromDb(String category, String orderId) {

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + category + "/" + dbId + "/" + orderId + "/getOrderMainFromDb",
				null, ProcessResult.class);
		if (processResult.getRetCode() == 0) {
			System.out.println(processResult.toString());
			OrderMain orderMain = (OrderMain)JsonUtil.fromJson((String)processResult.getResponseInfo(), OrderMain.class);
			//OrderMain orderMain = (OrderMain) processResult.getResponseInfo();
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
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" +nextOrderFlow.getCatetory() + "/" +  dbId + "/" + orderId + "/stepjumping",
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
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" +orderFlow.getCatetory()+ "/" + dbId + "/" + orderId + "/updateStepStatus",
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
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + orderFlow.getCatetory() + "/" + dbId + "/" + orderId + "/updateStepStatus",
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
	public ProcessResult selectOrderContextDataFromDb(String category, String orderId, List<String> keys) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		JsonRequest jsonRequest = new JsonRequest();
		jsonRequest.setJsonString(JsonUtil.toJson(keys));
		processResult = restTemplate.postForObject(
				httpOrderDbUrl + "/" + category + "/" + dbId + "/" + orderId + "/getContextData", jsonRequest,
				ProcessResult.class);
		 if(processResult.getRetCode()==OrderAccessConst.RESULT_Success)
		 {
		 Map<String,String> contextMaps =
		 JsonUtil.fromJson((String)processResult.getResponseInfo(),new TypeToken<HashMap<String,String>>(){}.getType());
		
		 processResult.setResponseInfo(contextMaps);
		 }
		return processResult;
	}

	/**
	 * 新增订单上下文信息
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/orderDb/{dbId}/{orderId}/putContextData
	 * @param contextDatas
	 * @return
	 */
	public ProcessResult putOrderContextDataToDb(OrderMainContext orderMainContext) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = orderMainContext.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" + orderMainContext.getCatetory() + "/" + dbId + "/" + orderId + "/putContextData",
				orderMainContext, ProcessResult.class);
		return processResult;
	}

	
	/**
	 * @param jsonRequest
	 *            {"stepId":"xxxxxxxxxxxxxxx","flowId":"xxxxxxxxxxx"}
	 * @param orderId
	 * @return
	 */
	public ProcessResult selectOrderFlow(JsonRequest jsonRequest, String category,String orderId) {
		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpOrderDbUrl + "/" +category + "/" + dbId + "/" + orderId + "/selectOrderFlow",
				jsonRequest, ProcessResult.class);
		if(processResult.getRetCode()==OrderAccessConst.RESULT_Success)
		{
			processResult.setResponseInfo(JsonUtil.fromJson((String)processResult.getResponseInfo(), OrderFlow.class));
		}
		return processResult;
	}

	/**
	 * 根据orderId查询userOrders ownerKey createTime orderId三属性确定一条记录
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/selectUserOrdersById
	 * @param userOrders
	 * @return
	 
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
*/
	/**
	 * 根据状态查询userOrders ownerKey createTime currentStatus三属性确定一条记录
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/selectUserOrdersByStatus
	 * @param userOrders
	 * @return
	
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
	 */
	
	
	/**
	 * 根据orderId更新userOrders的状态
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/updateUserOrdersStatus
	 * @param userOrders
	 * @return
	 
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
*/
	/**
	 * 插入一条userOrders
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/insertUserOrders
	 * @param userOrders
	 * @return
	 
	public ProcessResult insertUserOrders(UserOrders userOrders) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String orderId = userOrders.getOrderId();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/insertUserOrders",
				userOrders, ProcessResult.class);
		return processResult;
	}
*/
	/**
	 * 删除一条userOrders
	 * 
	 * @param url
	 *            http://127.0.0.1:8088/userOrders/{dbId}/{orderId}/deleteUserOrders
	 * @return
	 
	public ProcessResult deleteUserOrders(String orderId) {
		// TODO Auto-generated method stub

		ProcessResult processResult = new ProcessResult();
		String dbId = OrderMain.getDbId(orderId);
		processResult = restTemplate.postForObject(httpUserOrdersUrl + "/" + dbId + "/" + orderId + "/deleteUserOrders",
				null, ProcessResult.class);
		return processResult;
	}
	*/
}
