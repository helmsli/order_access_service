package com.company.orderAccess.service.impl;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderAccess.service.OrderManagerService;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderMainContext;
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderIDServiceImplTest {
	 @Autowired  
	 private ApplicationContext context;
	
	 @Resource(name="orderIDService")  
	 private OrderIDServiceImpl orderIDServiceImpl;
	 
	 @Resource(name="orderManagerService")
	 private OrderManagerService orderManagerService;
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateOrderId() {
		//fail("Not yet implemented");
		for(int i=0;i<10000;i++)
		{
			ProcessResult processResult = orderIDServiceImpl.createOrderId("test","aa",null);
			System.out.print(processResult.toString());
		}
	}
	@Test
	
	public void testCreateOrder()
	{
		String category = "test";
		ProcessResult processResult = orderIDServiceImpl.createOrderId(category,"aa",null);
		if(processResult.getRetCode()!=OrderAccessConst.RESULT_Success)
		{
			fail("testCreateOrder : error create orderId");
			return ;
		}
		
		OrderMainContext  orderMainContext = new OrderMainContext();
		orderMainContext.setOrderId(processResult.getResponseInfo().toString());
		orderMainContext.setCatetory(category);
		orderManagerService.createOrder(orderMainContext);
		orderManagerService.startOrder(category, orderMainContext.getOrderId());
	}
	@Test
	public void testStartOrder()
	{
		String category = "test";
		
		
		OrderMainContext  orderMainContext = new OrderMainContext();
		orderMainContext.setOrderId("2801000001");
		orderMainContext.setCatetory(category);
		orderManagerService.createOrder(orderMainContext);
		orderManagerService.startOrder(category, orderMainContext.getOrderId());
	}
	
	@Test
	public void testSplit()
	{
		String singleStepStr="10,20,2,30";
		String[] strStepJumpDefInfo = StringUtils.tokenizeToStringArray(singleStepStr, ",");
		System.out.println(strStepJumpDefInfo.length);
		for(int i=0;i<strStepJumpDefInfo.length;i++)
		{
			System.out.println("abb:" + strStepJumpDefInfo[i]);
		}
	}
}
