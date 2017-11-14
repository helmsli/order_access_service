package com.company.orderAccess.controller.rest;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderAccess.serverManager.impl.DbOrderTaskService;
import com.company.orderAccess.service.OrderManagerService;
import com.google.gson.reflect.TypeToken;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.Const.OrderDbConst;
import com.xinwei.orderDb.domain.OrderFlow;
import com.xinwei.orderDb.domain.OrderMain;
import com.xinwei.orderDb.domain.OrderMainContext;

@RestController
@RequestMapping("/orderGateway")
public class OrderManagerController {
	@Resource(name="orderManagerService")
	private OrderManagerService orderManagerService;
	@Resource(name="dbOrderTaskService")
	private DbOrderTaskService dbOrderTaskService;

	@RequestMapping(method = RequestMethod.POST, value = "/{category}/{dbId}/{orderId}/createOrder")
	public ProcessResult addOrderMain(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId, @RequestBody OrderMainContext orderMain) {
		ProcessResult processResult = new ProcessResult();
		try {
			processResult = orderManagerService.createOrder(orderMain);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);

		}
		return processResult;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{category}/{dbId}/{orderId}/getOrder")
	public ProcessResult getOrderMain(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId) {
		ProcessResult processResult = new ProcessResult();
		try {

			OrderMain orderMain = this.dbOrderTaskService.getOrderMain(category, orderId);
			processResult.setResponseInfo(orderMain);
			processResult.setRetCode(OrderAccessConst.RESULT_Success);
			this.toJsonProcessResult(processResult);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderDbConst.RESULT_HandleException);

		}
		return processResult;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{category}/{dbId}/{orderId}/startOrder")
	public ProcessResult startOrderMain(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId) {
		ProcessResult processResult = new ProcessResult();
		try {
			
			processResult = orderManagerService.startOrder(category, orderId,null);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);

		}
		return processResult;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{category}/{dbId}/{orderId}/startOrder")
	public ProcessResult startOrderMainWithParm(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId,@RequestBody JsonRequest jsonRequest) {
		ProcessResult processResult = new ProcessResult();
		try {
			
			processResult = orderManagerService.startOrder(category, orderId,jsonRequest);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);

		}
		return processResult;
	}
	@RequestMapping(method = RequestMethod.POST, value = "/{category}/{dbId}/{orderId}/putContextData")
	public ProcessResult putContextData(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId, @RequestBody OrderMainContext orderMainContext) {
		ProcessResult processResult = new ProcessResult();
		try {

			processResult=this.dbOrderTaskService.putOrderMainContext(orderMainContext);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);

		}
		return processResult;
	}
     
	@RequestMapping(method = RequestMethod.POST, value = "/{category}/{dbId}/{orderId}/getContextData")
	public ProcessResult getContextData(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId, @RequestBody JsonRequest jsonRequest) {
		ProcessResult processResult = new ProcessResult();
		try {
			String jsonString = jsonRequest.getJsonString();
			List<String> jsonList = JsonUtil.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
			
			
			processResult =this.dbOrderTaskService.getOrderMainContext(category, orderId, jsonList);
			toJsonProcessResult(processResult);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Success);
		}
		return processResult;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{category}/{dbId}/{orderId}/mJumpToNext")
	public ProcessResult manualJumpToNextStep(@PathVariable String category, @PathVariable String dbId,
			@PathVariable String orderId, @RequestBody OrderFlow orderFlow) {
		ProcessResult processResult = new ProcessResult();
		try {
			
			
			processResult =this.orderManagerService.mjumpToNext(category,orderFlow);
			toJsonProcessResult(processResult);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderDbConst.RESULT_HandleException);
		}
		return processResult;
	}

	protected void toJsonProcessResult(ProcessResult processResult) {
		if (processResult.getRetCode() == OrderDbConst.RESULT_SUCCESS) {

			Object object = processResult.getResponseInfo();
			if (object != null) {
				processResult.setResponseInfo(JsonUtil.toJson(object));
			}
		}
	}
}
