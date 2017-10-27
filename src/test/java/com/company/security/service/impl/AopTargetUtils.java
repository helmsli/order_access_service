package com.company.security.service.impl;

public class AopTargetUtils {
	 /** 
	57      * 获取 目标对象 
	58      * @param proxy 代理对象 
	59      * @return  
	60      * @throws Exception 
	61     
	public static Object getTarget(Object proxy) throws Exception {
		if(!AopUtils.isAopProxy(proxy)) {
			return proxy;//不是代理对象  
	65         }  
	66         if(AopUtils.isJdkDynamicProxy(proxy)) {  
	67             return getJdkDynamicProxyTargetObject(proxy);  
	68         } else { //cglib  
	69             return getCglibProxyTargetObject(proxy);  
	70         }  
	71     }  
	72  
	73     private static Object getCglibProxyTargetObject(Object proxy) throws Exception {  
	74         Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");  
	75         h.setAccessible(true);  
	76         Object dynamicAdvisedInterceptor = h.get(proxy);  
	77         Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");  
	78         advised.setAccessible(true);  
	79         Object target = ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();  
	80         return target;  
	81     }  
	82  
	83  
	84     private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {  
	85         Field h = proxy.getClass().getSuperclass().getDeclaredField("h");  
	86         h.setAccessible(true);  
	87         AopProxy aopProxy = (AopProxy) h.get(proxy);  
	88         Field advised = aopProxy.getClass().getDeclaredField("advised");  
	89         advised.setAccessible(true);  
	90         Object target = ((AdvisedSupport)advised.get(aopProxy)).getTargetSource().getTarget();  
	91         return target;  
	92     }
	 */  
}
