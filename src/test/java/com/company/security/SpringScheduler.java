package com.company.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

public class SpringScheduler implements SchedulingConfigurer{
	 @Autowired  
	 private ApplicationContext context;
	private static final Logger logger = LoggerFactory.getLogger(SpringScheduler.class);  
    
    private static String cron;  
      
    public SpringScheduler() {  
        cron = "0/5 * * * * ?";  
          
        // 开启新线程模拟外部更改了任务执行周期  
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                try {  
                    Thread.sleep(15 * 1000);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
                  
                cron = "0/10 * * * * ?";  
                System.err.println("cron change to: " + cron);  
            }  
        }).start();  
    }  
  
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		 taskRegistrar.addTriggerTask(new Runnable() {  
	            @Override  
	            public void run() {  
	                // 任务逻辑  
	                logger.debug("dynamicCronTask is running...");  
	            }  
	        }, new Trigger() {  
	            

				@Override
				public Date nextExecutionTime(TriggerContext triggerContext) {
				     // 任务触发，可修改任务的执行周期  
	                CronTrigger trigger = new CronTrigger(cron);  
	                Date nextExec = trigger.nextExecutionTime(triggerContext);  
	                return nextExec;  
	           
				}  
	        });  

	}
	
	public void createCron()
	{
		
		ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) context.getBean("scheduler");  
		scheduler.schedule(task, startTime)
		CronTrigger trigger = new CronTrigger("0 0/5 * * * ?"); // Every 5 minutes  
		ScheduledFuture<Object> scedulefuture = scheduler.schedule(taskObject, trigger);  
	}
}
