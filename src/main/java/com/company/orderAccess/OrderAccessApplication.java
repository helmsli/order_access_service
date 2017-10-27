package com.company.orderAccess;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
//@EnableRedisHttpSession
//@EnableRedisHttpSession 
@ComponentScan ("com.company.security")
@MapperScan ("com.company.security.mapper")
@ImportResource ({ "classpath:hessian/hessian-client.xml", "classpath:hessian/hessian-server.xml" })
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400*30)
public class OrderAccessApplication {
    public static  ApplicationContext  app = null;
	public static void main(String[] args) {
		app = SpringApplication.run(OrderAccessApplication.class, args);
		
		try {
			
		}
		catch(Throwable e)
		{
		   e.printStackTrace();	
		}
	}
}
