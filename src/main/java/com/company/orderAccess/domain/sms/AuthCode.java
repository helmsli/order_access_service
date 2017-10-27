package com.company.orderAccess.domain.sms;

import java.io.Serializable;

public class AuthCode implements Serializable{
	
	//自适应，服务器决定
	public static final int SendType_Auto= 0;
	/**
	 * 短信发送方式
	 */
	public static final int SendType_SMS = 1;
	//语音发送方式
	public static final int SendType_voice = 2;
	
	/**加密方式，rsa*/
	public static final int CrcType_RSA = 0;
	
	private String phone;
	/**
	 * 短信交易号
	 */
	private String transid;
	/**
	 * 发送的序号
	 */
	private String sendSeqno;
	/**
	 * 校验码
	 */
	private String authCode;
	/**
	 * 校验码发送给客户端的方式
	 */
	private int sendType;
	/**
	 * 加密方式
	 */
	private int crcType;
	/**
	 * 随机数
	 */
	private String random;
	/**
	 * 秘钥
	 */
	private String publicKey;
		
	public String getTransid() {
		return transid;
	}
	public void setTransid(String transid) {
		this.transid = transid;
	}
	public String getSendSeqno() {
		return sendSeqno;
	}
	public void setSendSeqno(String sendSeqno) {
		this.sendSeqno = sendSeqno;
	}
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAuthCode() {
		return authCode;
	}
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
	
	
	
	public int getSendType() {
		return sendType;
	}
	public void setSendType(int sendType) {
		this.sendType = sendType;
	}
	public int getCrcType() {
		return crcType;
	}
	public void setCrcType(int crcType) {
		this.crcType = crcType;
	}
	public String getRandom() {
		return random;
	}
	public void setRandom(String random) {
		this.random = random;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public boolean isSaveAuthCode(AuthCode destAuthCode)
	{
		try {
			return this.authCode.equalsIgnoreCase(destAuthCode.getAuthCode())&& this.transid.equalsIgnoreCase(destAuthCode.getTransid()) && this.phone.equalsIgnoreCase(destAuthCode.getPhone());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		return "AuthCode [phone=" + phone + ", transid=" + transid + ", sendSeqno=" + sendSeqno + ", authCode="
				+ authCode + ", sendType=" + sendType + ", crcType=" + crcType + ", random=" + random + ", publicKey="
				+ publicKey + "]";
	}
	
	
	
}
