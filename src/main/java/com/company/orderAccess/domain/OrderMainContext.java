package com.company.orderAccess.domain;

import java.util.Map;

import com.xinwei.orderDb.domain.OrderMain;

public class OrderMainContext extends OrderMain {
	private static final long serialVersionUID = -2486359166067173166L;
	
	/**
	 * 保存map的json
	 */
	private String contextDatasJson;
	
	/**
	 * 保存map对象
	 */
	private Map<String, String> contextDatasMap;


	public String getContextDatasJson() {
		return contextDatasJson;
	}


	public void setContextDatasJson(String contextDatasJson) {
		this.contextDatasJson = contextDatasJson;
	}


	public Map<String, String> getContextDatasMap() {
		return contextDatasMap;
	}


	public void setContextDatasMap(Map<String, String> contextDatasMap) {
		this.contextDatasMap = contextDatasMap;
	}

	
	
}
