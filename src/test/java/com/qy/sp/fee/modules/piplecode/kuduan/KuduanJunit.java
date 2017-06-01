package com.qy.sp.fee.modules.piplecode.kuduan;

import java.math.BigDecimal;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;

public class KuduanJunit {
	String baseURL = "http://127.0.0.1:8180/SPFee";
//	String baseURL = "http://123.56.158.156:8318/spfee";

	@Test // 调试回调接口
	public void testSync() {
		try {
			String channelUrl = baseURL + "/piple/kuduan/sync";
			String spnumber = "100680000";
			String mobile = "17300000000";
			String status = "DELIVRD";
			String msg = "扣费成功";
			String link_id = "145861317030956543198504";
			String fee = "300";
			String paytime = "17-12-12 1:1:1";
			String param = "spnumber=" + spnumber + "&mobile=" + mobile + "&status=" + status + "&msg=" + msg
					+ "&fee=" + fee+"&link_id="+link_id+"&paytime="+paytime;
			String ackUrl = channelUrl + "?" + param;
			String result = HttpClientUtils.doGet(ackUrl, "UTF-8");
			System.out.println("result:\n" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test // 通知渠道
	public void testNotify() {
		try {
			String channelUrl = "http://your_server_address/test.jsp";
			String orderId = "14586131703095654319859";
			String mobile = "17372238417";
			String status = "DELIVRD";
			double amount = new BigDecimal(100 / 100.0).doubleValue();
			String productCode = "P00100";
			String pipleKey = "PM1081";
			String apiKey = "1003";
			String imsi = "460012168618225";
			String extData = null;
			String param = "orderId=" + orderId + "&mobile=" + mobile + "&status=" + status + "&productCode="
					+ productCode + "&pipleKey=" + pipleKey + "&apiKey=" + apiKey + "&amount=" + amount + "&imsi=" + imsi
					+ "&extData=" + extData;
			String ackUrl = channelUrl + "?" + param;
			String rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
			System.out.println(rst);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
