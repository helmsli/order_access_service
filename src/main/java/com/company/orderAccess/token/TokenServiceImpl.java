package com.company.orderAccess.token;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


@Service("tokenService")
public class TokenServiceImpl implements TokenService{

	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	public  final String Key_prefix_TokenExpired= "luExpir:";
	
	private int durationSeconds = 24*3600;
	
	@Value("${token.expireSeconds:1800}")  
	private int tokenExpireSeconds;
	/**
	 * 获取用户安全级别的token
	 * @param token
	 * @return
	 */
	public  String getTokenAccessKey(String token)
	{
		StringBuilder str= new StringBuilder();
		str.append(Key_prefix_TokenExpired);
		str.append(token);
		return str.toString();
	}
	@Override
	public boolean setTokenInfo(String token, TokenInfo tokenInfo, int duartionSeconds) {
		// TODO Auto-generated method stub
		String accessKey  =getTokenAccessKey(token);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		tokenInfo.setCreateTime(System.currentTimeMillis());
		opsForValue.set(accessKey, tokenInfo,duartionSeconds,TimeUnit.SECONDS);
		this.durationSeconds = duartionSeconds;
		return true;

	}

	@Override
	public TokenInfo getTokenInfo(String token) {
		// TODO Auto-generated method stub
		String accessKey  =getTokenAccessKey(token);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		TokenInfo tokenInfo = (TokenInfo)opsForValue.get(accessKey);
		if(tokenInfo==null)
		{
			return null;
		}
		else
		{
			if(System.currentTimeMillis() - tokenInfo.getCreateTime()>tokenExpireSeconds)
			{
				return null;
			}
			if(System.currentTimeMillis() - tokenInfo.getCreateTime()>600)
			{
				setTokenInfo(token,tokenInfo,durationSeconds);
			}
		}
		return tokenInfo;
	}

	@Override
	public TokenInfo checkTokenInfo(String token) {
		// TODO Auto-generated method stub
		String accessKey  =getTokenAccessKey(token);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
		TokenInfo tokenInfo = (TokenInfo)opsForValue.get(accessKey);
		if(tokenInfo==null)
		{
			return null;
		}
		else
		{
			if(System.currentTimeMillis() - tokenInfo.getCreateTime()>tokenExpireSeconds)
			{
				return null;
			}
			if(System.currentTimeMillis() - tokenInfo.getCreateTime()>600)
			{
				setTokenInfo(token,tokenInfo,durationSeconds);
			}
		}
		return tokenInfo;
	}
	@Override
	public boolean delTokenInfo(String token) {
		// TODO Auto-generated method stub
		return true;
	}

}
