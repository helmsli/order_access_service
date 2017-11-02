package com.company.orderAccess.controller.rest;

import java.security.PrivateKey;

import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderAccess.service.OrderIDService;
import com.xinwei.nnl.common.domain.JsonRequest;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderMain;

@RestController
@RequestMapping("/orderId")
@ConditionalOnProperty(name = "order.idService.enable")
public class OrderIDController {
	@Resource(name="orderIDService")
	private OrderIDService orderIDService;
	/**
	 * 申请订单ID
	 * @param countryCode
	 * @param jsonString
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST,value = "/{category}/{ownerKey}/createOrderId")
	public  ProcessResult createOrderId(@PathVariable String category,@PathVariable String ownerKey,@RequestBody JsonRequest jsonRequest) {
		ProcessResult processResult =new ProcessResult();
		processResult.setRetCode(OrderAccessConst.RESULT_Error_Fail);
		try {
			if(!StringUtils.isEmpty(ownerKey))
			{
				processResult = orderIDService.createOrderId(category, ownerKey, jsonRequest);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return processResult;
	}
	
	
	
}
