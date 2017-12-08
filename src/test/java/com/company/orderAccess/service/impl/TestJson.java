package com.company.orderAccess.service.impl;

import com.company.orderTask.domain.OrderTaskInDef;
import com.xinwei.nnl.common.util.JsonUtil;

public class TestJson {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String taskIn="{\"name\":\"等待付款\", \"category\":2}";
	    JsonUtil.fromJson(taskIn,OrderTaskInDef.class);
		
	}

}
