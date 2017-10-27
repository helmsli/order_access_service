package com.company.orderAccess.utils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 生成相应的公钥和私钥 
 * @author helmsli
 *
 */
public interface SecurityKeyService {
	
	/**
	 * 根据transid和电话号码获取私要
	 * @param transid
	 * @param phone
	 * @return
	 */
	public PrivateKey getPrivatekey(String transid,String phone);
	
	/**
	 * 
	 * @param transid
	 * @param phone
	 * @return
	 */
	public PublicKey getPublickey(String transid,String phone);
	
	
}
