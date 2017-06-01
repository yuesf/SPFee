package com.qy.sp.fee.modules.piplecode.legao;

import java.math.BigDecimal;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TProduct;

import net.sf.json.JSONObject;

public class LegaoJunit {
	String baseURL = "http://127.0.0.1:8180/SPFee";
	// String baseURL = "http://123.56.158.156:8318/spfee";

	// String baseURL = "http://www.chinaunigame.net/spfee";
	@Test // 获取验证码
	public void testGetSMS() {
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("mobile", "17621307365");
		parameters.put("pipleId", "14932063591761975987400");// 联通
		parameters.put("productCode", "P00100");
		parameters.put("imsi", "460012168618225");
		parameters.put("imei", "460012168618225");
		parameters.put("extData", "100080"); // channelid
		parameters.put("ipProvince", "上海");
		try {
			String result = HttpClientUtils.doPost(baseURL + "/channel/getSms", parameters.toString(),
					HttpClientUtils.UTF8);
			System.out.println("result:\n" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test // 验证验证码
	public void testVertifySMS() {
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003");
		parameters.put("pipleId", "14930401747244193434221");
		parameters.put("orderId", "14931024320777390802583");
		parameters.put("verifyCode", "5179");
		try {
			String result = HttpClientUtils.doPost(baseURL + "/channel/vertifySms", parameters.toString(),
					HttpClientUtils.UTF8);
			System.out.println("result:\n" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test // 调试回调接口
	public void testSync() {
		JSONObject parameters = new JSONObject();
		parameters.put("status", "0");
		// parameters.put("msg", "扣费成功");
		parameters.put("orderId", "14927421163816784364126");
		// parameters.put("price","4.00");
		// parameters.put("chargeCode","zl22098A00400");
		parameters.put("mobile", "17372238417");
		// parameters.put("transmissionData","14781604929965326430665");
		try {
			String result = HttpClientUtils.doPost(baseURL + "/piple/legao/sync", parameters.toString(),
					HttpClientUtils.UTF8);
			System.out.println("result:\n" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test//通知渠道
	public void testNotify() {
		try {
			String channelUrl = "http://your_server_address/test.jsp";
			String orderId = "14586131703095654319859";
			String mobile = "13058411561";
			String status = "ok";
			double amount = new BigDecimal(100/100.0).doubleValue();
			String productCode = "P00100";
			String pipleId = "14586131703095654319859";
			String apiKey = "1003";
			String imsi = "460012168618225";
			String extData = null;
			String param = "orderId=" + orderId + "&mobile=" + mobile + "&status=" + status + "&productCode=" + productCode
					+ "&pipleId=" + pipleId+"&apiKey="+apiKey+"&amount="+amount+"&imsi="+imsi+"&extData=" + extData;
			String ackUrl = channelUrl + "?" + param;
			String rst = HttpClientUtils.doGet(ackUrl, "UTF-8");
			System.out.println(rst);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
