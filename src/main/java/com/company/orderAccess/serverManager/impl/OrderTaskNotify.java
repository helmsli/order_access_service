package com.company.orderAccess.serverManager.impl;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.company.orderAccess.Const.OrderAccessConst;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.google.gson.reflect.TypeToken;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
@Service("orderTaskNotify")
public class OrderTaskNotify implements  InitializingBean{
	private  RestTemplate template = new RestTemplate();

	@Value("${order.task.newNotifyUrl}")
	private String gnewTalkNotifyUrl;
	
	@Value("${order.task.threadPoolMaxSize:100}")
	private int threadPoolMaxSize;
	@Value("${order.task.threadPoolInitSize:5}")
	private int threadPoolInitSize;
	@Value("${order.task.threadPoolKeepAliveSeconds:300}")
	private int threadPoolKeepAliveTime;
	@Value("${order.task.threadPoolQueneSize:1000}")
	private int threadPoolQueneSize;
	
	BlockingQueue<Runnable> workQueue =null;
	
	private ThreadPoolExecutor notifyTaskPool = null;
	
	/**
	 * 新任务通知服务
	 * @param orderTaskInfo
	 */
	public void notifyNewTask(OrderTaskRunInfo orderTaskInfo)
	{
		try {
			if(workQueue.size()>threadPoolQueneSize-10)
			{
				return;
			}
			notifyTaskPool.execute(new NotifyTalk(orderTaskInfo,this.template,this.gnewTalkNotifyUrl));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 class NotifyTalk implements Runnable{
		 private OrderTaskRunInfo orderTaskInfo;
		 private  RestTemplate notifyTemplate = null;
		 private  String newTalkNotifyUrl; 
		 public NotifyTalk(OrderTaskRunInfo orderTaskInfo,RestTemplate notifyTemplate,String newTalkNotifyUrl)
		 {
			 this.orderTaskInfo = orderTaskInfo;
			 this.notifyTemplate = notifyTemplate;
			 this.newTalkNotifyUrl = newTalkNotifyUrl;
		 }
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				notifyRunTalk();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/**
		 * 通知运行任务启动任务
		 * @return
		 */
		public  ProcessResult notifyRunTalk()
		{
			ProcessResult result = null;
			
			try {
				
				result  = template.postForObject(newTalkNotifyUrl + "/" + orderTaskInfo.getCatetory() + "/" +orderTaskInfo.getOrderId() + "/" +orderTaskInfo.getCurrentStep()+ "/runOrderTask"  ,orderTaskInfo, ProcessResult.class);
				if(result.getRetCode()!=OrderAccessConst.RESULT_Success)
				{
					
				}
				
				return result;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			return result;
		}
	      
	  }

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		workQueue =new ArrayBlockingQueue<Runnable>(threadPoolQueneSize);
		notifyTaskPool = new ThreadPoolExecutor(threadPoolInitSize, threadPoolMaxSize, threadPoolKeepAliveTime, TimeUnit.SECONDS,workQueue);
			
	}
}
