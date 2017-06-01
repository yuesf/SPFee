package com.qy.sp.fee.modules.piplecode.kuyue;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.junit.Test;

import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.modules.piplecode.kuyue.ParamsSign;

import net.sf.json.JSONObject;

public class KuyueJunit {

	String baseURL = "http://127.0.0.1:8180/SPFee";
//	String baseURL = "http://www.chinaunigame.net/spfee";

	@Test
	public void testGetSMS() {
		JSONObject parameters = new JSONObject();
		parameters.put("apiKey", "1003"); 
		parameters.put("mobile", "18114018572");
		parameters.put("pipleId", "14924845041064260113816"); //需要SPManger中申请的
		parameters.put("productCode", "P00100");//10元
		parameters.put("imsi", "460028529457924");
		parameters.put("ipProvince", "江苏");//SPManager中开通的省份
		parameters.put("extData", "zcz"); 
		
		try {
			String result = HttpClientUtils.doPost(baseURL + "/channel/getSms", parameters.toString(),
					HttpClientUtils.UTF8);
			System.out.println("result:\n" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
