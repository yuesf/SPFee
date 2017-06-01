package com.qy.sp.fee.modules.piplecode.kuyue;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.axis.utils.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


/**********************************************************
 * Copyright © 2008，北京福富软件技术股份有限公司
 * All Rights Reserved.
 *
 * 文件名称：HttpClientCommon.java
 *
 * 初始版本：1.0.0.0
 * 原 作 者： 林  娟
 * 完成日期： NOV 6, 2014 03:00 PM

 * 当前版本： 1.1.0.0
 * 作    者： 输入作者（或修改者）名字
 * 完成日期： 
 * 
 ************************************************************/
public class HttpClientCommon {
	
	public static int connectionTimeOut = 20000;
	public static int readTimeOut = 60000;

	public static String post(String url,NameValuePair[] data) throws Exception{
		return post(url, data, null) ;
	}
	
	public static String post(String url,NameValuePair[] data,Header[] headers) throws Exception{
		if(StringUtils.isEmpty(url)){
			throw new Exception("The url is empty!") ;
		}
		HttpClient httpClient = getHttpClient(true) ;
		HttpPost httpPost = new HttpPost(new URI(url.trim())) ;
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>() ;
		for(NameValuePair d : data){
			nameValuePairs.add(d) ;
		}
		try{
			if(null != headers && 0 != headers.length){
				httpPost.setHeaders(headers) ;
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8)) ;
			HttpResponse httpResponse = httpClient.execute(httpPost) ;
			int respCode = httpResponse.getStatusLine().getStatusCode() ;
			String respData = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
			if(respCode != HttpStatus.SC_OK){
				throw new Exception("Bad response status, " + respCode + "," + respData) ;
			}
			return respData ;
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	
	
	
	
	private static HttpClient getHttpClient(boolean useSSL){
		HttpClient httpClient = useSSL ? getHttpsClient() : new DefaultHttpClient() ;
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeOut);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeOut);
		return httpClient;
	}
	
	private static HttpClient getHttpsClient(){
		HttpClient httpClient = new DefaultHttpClient() ;
		SSLContext sslContext = null ;
		try {
			sslContext = SSLContext.getInstance("SSL") ;
			sslContext.init(null, new TrustManager[]{trustManager}, null) ;
			SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager clientConnectionManager = httpClient.getConnectionManager();
			SchemeRegistry schemeRegistry = clientConnectionManager.getSchemeRegistry();
			schemeRegistry.register(new Scheme("https", 443, sslSocketFactory));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return httpClient ;
	}
	
	
	private static X509TrustManager trustManager = new X509TrustManager() {
		public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		}
		public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		}
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};

	
}