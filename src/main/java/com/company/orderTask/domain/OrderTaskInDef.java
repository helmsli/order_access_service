package com.company.orderTask.domain;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;

public class OrderTaskInDef implements Serializable {
	
	
	//定时执行
	public static final int catogory_cron = 0;
	//立即执行
	public static final int category_mmediate = 1;
	//调用者自己手动执行
	public static final int catogory_manual = 2;
	
	private String name;
	private int  category;
	private String url;
	//cron express
	private String runExpress;
	//重做的retry次数
	private String retryExpress;
	
	
	public boolean isImmediateRun()
	{
		return category==category_mmediate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRunExpress() {
		return runExpress;
	}

	public void setRunExpress(String runExpress) {
		this.runExpress = runExpress;
	}

	public String getRetryExpress() {
		return retryExpress;
	}

	public void setRetryExpress(String retryExpress) {
		this.retryExpress = retryExpress;
	}

	
	@Override
	public String toString() {
		return "OrderTalkInDef [name=" + name + ", category=" + category + ", url=" + url + ", runExpress=" + runExpress
				+ ", retryExpress=" + retryExpress  + "]";
	}
	
}
