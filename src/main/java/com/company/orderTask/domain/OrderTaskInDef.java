package com.company.orderTask.domain;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;

public class OrderTaskInDef implements Serializable {
	
	
	//定时执行
	public static final int catogory_cron = 0;
	//立即执行
	public static final int category_immediate = 1;
	//调用者自己手动执行
	public static final int catogory_manual = 2;
	
	private String name;
	private int  category;
	private String url;
	//cron express
	private String runExpress;
	//重做的retry次数
	private String retryExpress;
	
	
	private int maxThreadNumber;
	
	private int initThreadNumber;
	
	private int keepAliveTime;
	
	private int queneSize;
	
	public boolean isImmediateRun()
	{
		return category==category_immediate;
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

	
	
	public int getMaxThreadNumber() {
		return maxThreadNumber;
	}

	public void setMaxThreadNumber(int maxThreadNumber) {
		this.maxThreadNumber = maxThreadNumber;
	}

	public int getInitThreadNumber() {
		return initThreadNumber;
	}

	public void setInitThreadNumber(int initThreadNumber) {
		this.initThreadNumber = initThreadNumber;
	}

	public int getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getQueneSize() {
		return queneSize;
	}

	public void setQueneSize(int queneSize) {
		this.queneSize = queneSize;
	}

	@Override
	public String toString() {
		return "OrderTaskInDef [name=" + name + ", category=" + category + ", url=" + url + ", runExpress=" + runExpress
				+ ", retryExpress=" + retryExpress + ", maxThreadNumber=" + maxThreadNumber + ", initThreadNumber="
				+ initThreadNumber + ", keepAliveTime=" + keepAliveTime + ", queneSize=" + queneSize + "]";
	}

	
	
}
