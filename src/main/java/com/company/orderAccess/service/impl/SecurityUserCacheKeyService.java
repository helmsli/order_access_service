package com.company.orderAccess.service.impl;

import org.springframework.util.StringUtils;

import com.company.orderAccess.utils.SecurityConst;

public class SecurityUserCacheKeyService {
	
	public static final String Key_prefix_LoginUser= "luser:";
	public static final String Key_prefix_phone= "lphone:";
	public static final String Key_prefix_email= "lemail:";
	public static final String Key_prefix_lastModify= "lmodify:";
	public static final String Key_prefix_UserLock= "lulock:";
	public static final String Key_prefix_DeviceType= "luDType:";
	public static final String Key_prefix_CreateUserid= "luserCreate:";
	public static final String Key_prefix_RandId= "luserRand:";
	
	
	public static final String Key_prefix_Split=":";
	
	/**
	 * 创建userID的key
	 * @return
	 */
	public String getCreateUserIdkey()
	{
		StringBuilder str= new StringBuilder();
		str.append(Key_prefix_CreateUserid);
		return str.toString();
	}
	
	/**
	 * 根据登录类型获取token的key
	 * @param loginType -- pad/mobile/web
	 * @param userid
	 * @return
	 */
	public String getTokenKey(int loginType,long userid)
	{
		StringBuilder str= new StringBuilder();
		str.append(Key_prefix_DeviceType);
		str.append(userid);
		str.append(Key_prefix_Split);
		str.append(loginType);
		return str.toString();
	}
	public String getTokenAccessKey(String token)
	{
		return SecurityConst.getTokenRediskey(token);
	}
	
	/**
	 * 获取同步锁的key
	 * @param userId
	 * @return
	 */
	public String getLockkey(long userId)
	{
		StringBuilder retStr = new StringBuilder();
		retStr.append(Key_prefix_UserLock);
		retStr.append(userId);
		return retStr.toString();
	}
	
	public String getUserSessionkey(long userId)
	{
		StringBuilder retStr = new StringBuilder();
		retStr.append(Key_prefix_UserLock);
		retStr.append(userId);
		return retStr.toString();
	}
	
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public String getLoginUserkey(long userId) throws Exception
	{
		if(userId>0)
		{
			StringBuilder retStr = new StringBuilder();
			retStr.append(Key_prefix_LoginUser);
			retStr.append(userId);
			return retStr.toString();
		}
		throw new Exception("userId is 0");
	}
	
	/**
	 * 
	 * @param phone
	 * @return
	 */
	public String getPhoneKey(String phone) throws Exception
	{
		if(!StringUtils.isEmpty(phone))
		{
			StringBuilder retStr = new StringBuilder();
			retStr.append(Key_prefix_phone);
			retStr.append(phone);
			return retStr.toString();
		}
		throw new Exception("phone is null");
	}
	/**
	 * 
	 * @param phone
	 * @return
	 */
	public String getEmailKey(String email) throws Exception
	{
		if(!StringUtils.isEmpty(email))
		{
			StringBuilder retStr = new StringBuilder();
			retStr.append(Key_prefix_email);
			retStr.append(email);
			return retStr.toString();
		}
		throw new Exception("email is null");
		
	}
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public String getLastModifyKey(long userId)
	{
		StringBuilder retStr = new StringBuilder();
		retStr.append(Key_prefix_lastModify);
		retStr.append(userId);
		return retStr.toString();
	}
	
	/**
	 * 
	 * @param userId
	 * @return
	 */
	public String getLastModifyKeyPhone(String phone)
	{
		StringBuilder retStr = new StringBuilder();
		//用两个分隔符和userid区分
		retStr.append(Key_prefix_lastModify);
		retStr.append(Key_prefix_lastModify);
		retStr.append(phone);
		return retStr.toString();
	}
	/**
	 * 获取用户的随机数
	 * @param loginId
	 * @param transid
	 * @return
	 */
	public String getRandomkey(String loginId,String transid)
	{
		StringBuilder retStr = new StringBuilder();
		retStr.append(Key_prefix_RandId);
		retStr.append(loginId);
		retStr.append(this.Key_prefix_Split);
		retStr.append(transid);
		return retStr.toString();
	}
}
