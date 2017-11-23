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
import com.company.orderTask.domain.OrderTaskInDef;
import com.company.orderTask.domain.OrderTaskRunInfo;
import com.google.gson.reflect.TypeToken;
import com.xinwei.nnl.common.domain.ProcessResult;
import com.xinwei.nnl.common.util.JsonUtil;
import com.xinwei.orderDb.domain.OrderFlowStepdef;
@Service("orderTaskNotifyScheduler")
public class OrderTaskNotifyScheduler implements  InitializingBean{
	@Autowired
	private  RestTemplate template;

	@Value("${order.task.runningNotifyUrl}")
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
			notifyTaskPool.execute(new OrderRunInTaskNotify(orderTaskInfo,this.template,this.gnewTalkNotifyUrl));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void notifyNewTrigger(OrderTaskInDef orderTaskInDef,OrderTaskRunInfo orderTaskInfo)
	{
		try {
			notifyTaskPool.execute(new OrderRunInTaskTrigger(orderTaskInDef,orderTaskInfo,this.template));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		workQueue =new ArrayBlockingQueue<Runnable>(threadPoolQueneSize);
		notifyTaskPool = new ThreadPoolExecutor(threadPoolInitSize, threadPoolMaxSize, threadPoolKeepAliveTime, TimeUnit.SECONDS,workQueue);
			
	}
}
