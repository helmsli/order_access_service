package com.company.orderAccess.utils;

public class SecurityConst {
	public static final String Key_prefix_TokenExpired= "luExpir:";
	/**
	 * 获取用户安全级别的token
	 * @param token
	 * @return
	 */
	public static String getTokenRediskey(String token)
	{
		StringBuilder str= new StringBuilder();
		str.append(Key_prefix_TokenExpired);
		str.append(token);
		return str.toString();
	}
}
