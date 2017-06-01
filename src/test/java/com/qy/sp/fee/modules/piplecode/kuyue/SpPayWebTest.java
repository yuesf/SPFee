package com.qy.sp.fee.modules.piplecode.kuyue;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

/**********************************************************
 * Copyright © 2014，北京福富软件技术股份有限公司
 * All Rights Reserved.
 *
 * 文件名称：SpPayWebTest.java 
 * 摘    要：
 *
 * 初始版本：1.0.0.0
 * 原 作 者：叶平平
 * 完成日期：2016-7-15 下午4:17:42
 * 
 ************************************************************/
public class SpPayWebTest {
	
	 @Test
	 public void web(){
		 String state="state";
		 String reback_url="http://www.baidu.com";
		 String url ="http://api.189.cn/sp/iap/web/v3.0.1";
		 String sp_app_key="296366870000251693"; 
		 String sp_app_secret="d94038cd25117424a0ce664aaf59947f";
		 String phone="15321050825";
		 String billing_no="90214776";
		 String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		 
		try {
			TreeMap<String,String> treeMap = new TreeMap<String,String>() ;
			 treeMap.put("app_key", sp_app_key);
			 treeMap.put("phone", phone);
			 treeMap.put("billing_no", billing_no);
			 treeMap.put("timestamp", timestamp);
			 String sign = ParamsSign.value(treeMap,sp_app_secret);
			 //注意这个地方
			 sign=URLEncoder.encode(sign,"UTF-8");
			 NameValuePair[] params = {
					new BasicNameValuePair("app_key",treeMap.get("app_key")),
					new BasicNameValuePair("phone",treeMap.get("phone")),
					new BasicNameValuePair("billing_no",treeMap.get("billing_no")),
					new BasicNameValuePair("state",state),
					new BasicNameValuePair("reback_url",reback_url),
					new BasicNameValuePair("sign",sign),
					new BasicNameValuePair("timestamp",treeMap.get("timestamp"))};
			String resp = HttpClientCommon.post(url, params) ;
			System.out.println(resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
}
