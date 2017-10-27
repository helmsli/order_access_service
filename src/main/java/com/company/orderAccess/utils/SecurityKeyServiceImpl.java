package com.company.orderAccess.utils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


@Service("securityKeyService")
public class SecurityKeyServiceImpl implements SecurityKeyService {
	@Resource (name = "redisTemplate")
	protected RedisTemplate<Object, Object> redisTemplate;
	
	
	private KeyPair rsaKeyPair = RSAUtils.generateKeyPair();
	
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	protected String getRedisPrivatekey(String transid, String phone)
	{
		StringBuilder str = new StringBuilder();
		str.append(transid.substring(0, RSAUtils.Length_ServeridNode));
		str.append("*");
		str.append(RSAUtils.Rsa_private_key);
		return str.toString();
		
	}
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	protected String getRedisPublickey(String transid, String phone)
	{
		StringBuilder str = new StringBuilder();
		str.append(transid.substring(0, RSAUtils.Length_ServeridNode));
		str.append("*");
		str.append(RSAUtils.Rsa_public_key);
		return str.toString();
		
	}
	
	@Override
	public PrivateKey getPrivatekey(String transid, String phone) {
		// TODO Auto-generated method stub
		String transidkey = getRedisPrivatekey(transid,phone);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();		
		PrivateKey privateKey = (PrivateKey)opsForValue.get(transidkey);
		if(privateKey==null)
		{
			String redisPublickey = getRedisPublickey(transid,phone);
			redisTemplate.delete(redisPublickey);
		}
		return privateKey;
	}

	@Override
	public PublicKey getPublickey(String transid, String phone) {
		// TODO Auto-generated method stub
		String transidkey = getRedisPublickey(transid,phone);
		ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();		
		PublicKey publicKey = (PublicKey)opsForValue.get(transidkey);
		if(publicKey==null)
		{
			publicKey = rsaKeyPair.getPublic();
			PrivateKey PrivateKey =rsaKeyPair.getPrivate();
			opsForValue.set(transidkey, publicKey);
			String privateKeyStr = getRedisPrivatekey(transid,phone);			
			opsForValue.set(privateKeyStr, PrivateKey);			
		}
		return publicKey;
	}

}
