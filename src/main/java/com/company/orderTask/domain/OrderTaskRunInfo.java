package com.company.orderTask.domain;

import java.io.Serializable;
import java.util.Date;

public class OrderTaskRunInfo implements Serializable,Cloneable {
	/** 订单编号. */
	private String orderId;

	/** 订单类型. */
	private String catetory;
	/** 当前步骤. */
	private String currentStep;

	/** 当前步骤状态. */
	private int currentStatus;
	/** 流程ID. */
	private String flowId;

	private long expireTime;
	/**
	 * 运行次数
	 */
	private int runtimes;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCatetory() {
		return catetory;
	}

	public void setCatetory(String catetory) {
		this.catetory = catetory;
	}

	public String getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(String currentStep) {
		this.currentStep = currentStep;
	}

	public int getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(int currentStatus) {
		this.currentStatus = currentStatus;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public int getRuntimes() {
		return runtimes;
	}

	public void setRuntimes(int runtimes) {
		this.runtimes = runtimes;
	}

	@Override
	public String toString() {
		return "OrderTaskRunInfo [orderId=" + orderId + ", catetory=" + catetory + ", currentStep=" + currentStep
				+ ", currentStatus=" + currentStatus + ", flowId=" + flowId + ", expireTime=" + expireTime
				+ ", runtimes=" + runtimes + "]";
	}

	
	
	
}
