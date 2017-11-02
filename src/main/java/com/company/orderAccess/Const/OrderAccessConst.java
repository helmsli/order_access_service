package com.company.orderAccess.Const;

public class OrderAccessConst {
	public static final int RESULT_Success = 0;	
	public static final int RESULT_Error_Fail = 7000;	
	
	//范围6101到7000
	public static final int RESULT_ERROR_START = 6101;
	/**
	 * 订单不存在
	 */
	public static final int RESULT_ERROR_OrderNotExist = RESULT_ERROR_START+0;
	
	/**
	 * 定义已经启动或者运行中
	 */
	public static final int RESULT_ERROR_OrderHavedRunning = RESULT_ERROR_START+1;
	
	/**
	 * 没有运行的任务 
	 */
	public static final int RESULT_ERROR_noRunningTask = RESULT_ERROR_START+2;
	
	/**
	 * can not find the step information 
	 */
	public static final int RESULT_ERROR_noStepInfo = RESULT_ERROR_START+3;
	/**
	 * owner key is null
	 */
	public static final int RESULT_ERROR_ownerIdKeyNull = RESULT_ERROR_START+4;
	
}
