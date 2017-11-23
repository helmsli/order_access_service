package com.company.orderTask.domain;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
/**
 * 保存到订单中的taskin里面
 * @author helmsli
 *
 */
public class OrderTaskInDef implements Serializable {
	
		//定时执行
	public static final int catogory_cron = 0;
	//立即执行
	public static final int category_immediate = 1;
	//调用者自己手动执行,后台可以配置cron，但是调用后不会通知调度任务
	public static final int catogory_manual = 2;
	
	private String name;
	private int  category;
	//配置到ip，端口和工程名子
	private String url;
	//rest的地址调用后的方法名称，具体的地址为url/category/dbid/orderid/restMethod
	private String restMethod;
	private String triggerUrl;
	private String triggerRestMethod;
	//cron express
	private String runExpress;
	//重做的retry次数
	private String retryExpress;
	
	//任务的最大线程数
	private int maxThreadNumber;
	//初始的线程树木
	private int initThreadNumber;
	//线程池的保持时间
	private int keepAliveTime;
	//线程池的队列大小
	private int queneSize;
	/**
	 * 任务调度时间间隔，单位毫秒
	 */
	private int timeoutMills=30000;
	
	
	
	public int getTimeoutMills() {
		return timeoutMills;
	}

	public void setTimeoutMills(int timeoutMills) {
		this.timeoutMills = timeoutMills;
	}

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


	public String getRestMethod() {
		return restMethod;
	}

	public void setRestMethod(String restMethod) {
		this.restMethod = restMethod;
	}

	
	public String getTriggerUrl() {
		return triggerUrl;
	}

	public void setTriggerUrl(String triggerUrl) {
		this.triggerUrl = triggerUrl;
	}

	public String getTriggerRestMethod() {
		return triggerRestMethod;
	}

	public void setTriggerRestMethod(String triggerRestMethod) {
		this.triggerRestMethod = triggerRestMethod;
	}

	@Override
	public String toString() {
		return "OrderTaskInDef [name=" + name + ", category=" + category + ", url=" + url + ", restMethod=" + restMethod
				+ ", triggerUrl=" + triggerUrl + ", triggerRestMethod=" + triggerRestMethod + ", runExpress="
				+ runExpress + ", retryExpress=" + retryExpress + ", maxThreadNumber=" + maxThreadNumber
				+ ", initThreadNumber=" + initThreadNumber + ", keepAliveTime=" + keepAliveTime + ", queneSize="
				+ queneSize + ", timeoutMills=" + timeoutMills + "]";
	}

	



	
	
}
