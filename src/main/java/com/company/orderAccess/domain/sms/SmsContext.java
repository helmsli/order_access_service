package com.company.orderAccess.domain.sms;

import java.io.Serializable;

public class SmsContext implements Serializable{
	
	private AuthCode smsValidCode;
	
    private String  authCode;
    
	public AuthCode getSmsValidCode() {
		return smsValidCode;
	}

	public void setSmsValidCode(AuthCode smsValidCode) {
		this.smsValidCode = smsValidCode;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
	
}
