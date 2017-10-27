package com.company.orderAccess.domain;

import java.io.Serializable;

public class OrderIdCache implements Serializable {
	/**
	 * 本地生成的id，如果cache出错会使用本地的id
	 */
	private long localNewOrderId=0;
	
	/**
	 * 剩余的uid个数；
	 */
	private int remainOrderidNumber=0;
	
	/**
	 * 新申请的OrderID的起始id
	 */
	private long startNewOrderId=0;

	public long getLocalNewOrderId() {
		return localNewOrderId;
	}

	public void setLocalNewOrderId(long localNewOrderId) {
		this.localNewOrderId = localNewOrderId;
	}

	public int getRemainOrderidNumber() {
		return remainOrderidNumber;
	}

	public void setRemainOrderidNumber(int remainOrderidNumber) {
		this.remainOrderidNumber = remainOrderidNumber;
	}

	public long getStartNewOrderId() {
		return startNewOrderId;
	}

	public void setStartNewOrderId(long startNewOrderId) {
		this.startNewOrderId = startNewOrderId;
	}
	
	
}
