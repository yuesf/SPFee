/**
 * 
 */
package com.qy.sp.fee.modules.piplecode.kuyue;

import com.qy.sp.fee.dto.TOrder;

/**
 * @author yuesf 2017年4月18日
 *
 */
public class KuyueOrder extends TOrder {
	private String smsVertifyUrl;//验证接口
	private String verifyCode;//验证码
	public String getSmsVertifyUrl() {
		return smsVertifyUrl;
	}
	public void setSmsVertifyUrl(String smsVertifyUrl) {
		this.smsVertifyUrl = smsVertifyUrl;
	}
	public String getVerifyCode() {
		return verifyCode;
	}
	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}
	
}
