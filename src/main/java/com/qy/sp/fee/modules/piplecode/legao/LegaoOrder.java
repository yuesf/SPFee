package com.qy.sp.fee.modules.piplecode.legao;

import com.qy.sp.fee.dto.TOrder;

public class LegaoOrder extends TOrder {
	private String smsVertifyUrl;//验证接口
	private String verifyCode;//验证码
	private String DSOrderId;//东硕外部orderId
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
	public String getDSOrderId() {
		return DSOrderId;
	}
	public void setDSOrderId(String dSOrderId) {
		DSOrderId = dSOrderId;
	}
	
}
