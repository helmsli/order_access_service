package com.company.orderAccess.token;

public interface TokenService {
	/**
	 * 登录成功后设置token信息
	 * @param token
	 * @param tokenInfo
	 * @param duartionSeconds
	 * @return
	 */
	public boolean setTokenInfo(String token,TokenInfo tokenInfo,int duartionSeconds);
	/**
	 * 获取token信息，并更新token的时间戳
	 * @param token
	 * @return
	 */
	public TokenInfo  getTokenInfo(String token);	
	/**
	 * 校验token是否有效，并更新token的时间戳
	 * @param token
	 * @return
	 */
	public TokenInfo  checkTokenInfo(String token);
	/**
	 * 
	 * @param token
	 * @return
	 */
	public boolean  delTokenInfo(String token);	
	
}

