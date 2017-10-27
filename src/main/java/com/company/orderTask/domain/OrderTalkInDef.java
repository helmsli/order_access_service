package com.company.orderTask.domain;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;

public class OrderTalkInDef implements Serializable {
	//定时执行
	public static final int catogory_cron = 1;
	//立即执行
	public static final int category_mmediate = 0;
	private String name;
	private int  category;
	private String url;
	//cron express
	private String runExpress;
	//重做的retry次数
	private String retryExpress;
	private int threadPoolMaxSize;
	private int threadPoolInitSize;
	private int threadPoolKeepAliveTime;
	private int threadPoolQueneSize;
	
	public boolean isImmediateRun()
	{
		return category==category_mmediate;
	}
}
