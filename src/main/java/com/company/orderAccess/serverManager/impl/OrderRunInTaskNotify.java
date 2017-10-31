package com.company.orderAccess.serverManager.impl;

import org.springframework.web.client.RestTemplate;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.orderDb.domain.OrderMain;
/**
 * 订单进入步骤后，通知调度器进行任务调度
 * @author helmsli
 *
 */
public class OrderRunInTaskNotify implements Runnable {

	private OrderTaskRunInfo orderTaskInfo;
	 private  RestTemplate notifyTemplate = null;
	 private  String newTalkNotifyUrl; 
	 public OrderRunInTaskNotify(OrderTaskRunInfo orderTaskInfo,RestTemplate notifyTemplate,String newTalkNotifyUrl)
	 {
		 this.orderTaskInfo = orderTaskInfo;
		 this.notifyTemplate = notifyTemplate;
		 this.newTalkNotifyUrl = newTalkNotifyUrl;
	 }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		int index=0;
		while(index<3)
		{
			try {
				index++;
				notifyRunTalk();
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 通知运行任务启动任务
	 * @return
	 */
	public  ProcessResult notifyRunTalk() throws Exception
	{
		ProcessResult result = null;
		
		try {
			
			result  = notifyTemplate.postForObject(newTalkNotifyUrl + "/" + orderTaskInfo.getCatetory() + "/" +  OrderMain.getDbId(orderTaskInfo.getOrderId()) + "/" +orderTaskInfo.getOrderId() + "/" +orderTaskInfo.getCurrentStep()+ "/runOrderTask"  ,orderTaskInfo, ProcessResult.class);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			throw e;
			
		}
		
	}

}
