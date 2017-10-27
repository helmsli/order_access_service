package com.company.orderAccess.controller.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.company.orderAccess.Const.OrderAccessConst;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.Const.OrderDbConst;
import com.xinwei.orderDb.domain.OrderMain;

@RestController
@RequestMapping("/order")
public class OrderManagerController {
	@RequestMapping(method = RequestMethod.POST, value = "{category}/{dbId}/{orderId}/createOrder")
	public ProcessResult addOrderMain(@PathVariable String category,@PathVariable String dbId, @PathVariable String orderId,
			@RequestBody OrderMain orderMain) {
		ProcessResult processResult = new ProcessResult();
		try {
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);

		}
		return processResult;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "{category}/{dbId}/{orderId}/startOrder")
	public ProcessResult startOrderMain(@PathVariable String category,@PathVariable String dbId, @PathVariable String orderId) {
		ProcessResult processResult = new ProcessResult();
		try {
			
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);

		}
		return processResult;
	}
}
