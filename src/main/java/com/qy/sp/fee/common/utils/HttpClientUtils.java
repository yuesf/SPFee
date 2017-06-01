package com.qy.sp.fee.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpClientUtils {

	private static Logger log = Logger.getLogger(HttpClientUtils.class);
	public final static String UTF8 = "UTF-8";
	public final static String GBK = "GBK";
	public final static String ISO = "ISO-8859-1";
	private final static int TIME_OUT = 10000;  //超时时间设置 默认10秒
	private static int connectTimeOut = 5000; // 连接超时
	private static int readTimeOut = 10000; //读取数据超时
	public static String requestEncoding = "UTF-8";

	private static PoolingHttpClientConnectionManager cm;
	private static RequestConfig requestConfig;
	
	static{
		cm = new PoolingHttpClientConnectionManager();
		//Increase max total connection to 200  
		cm.setMaxTotal(200);//总共保持200个连接(对于多个通过该http访问的网站)
		//Increase default max connection per route to 20  
		cm.setDefaultMaxPerRoute(20); //每个网站的默认连接最多20个
		//Increase max connections for localhost:80 to 50
		//连接池里面可以保持长连接到"http://open.wo.com.cn/"地址的最大数是50个,如果你请求"http://open.wo.com.cn/"这个地址的量很大，
		//把50个HTTP连接都占完了，那新的请求过来就需要等到其他使用连接池里面到这个地址的HTTP连接释放了才行
		//HttpHost wo = new HttpHost("http://open.wo.com.cn/", 80);
		//cm.setMaxPerRoute(new HttpRoute(wo), 80);
		
		//设置超时时间
		requestConfig = RequestConfig.custom()
				.setSocketTimeout(TIME_OUT)
				.setConnectTimeout(TIME_OUT)
				.build();
	}
	
	/**
	 * 发送POST请求
	 * @param url 请求地址
	 * @param json 请求数据JSON格式
	 * @param platID
	 * @param platPwd
	 * @return
	 * @throws Exception
	 */
	public static String doPost(String url, String json, String platID, String platPwd,String encode) throws Exception {
		CloseableHttpClient httpclient = bulidHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json;charset="+encode+"");
		httpPost.setHeader("Authorization", "platformID=\""+platID+"\",password=\""+platPwd+"\"");

		StringEntity entity = new StringEntity(json, encode);
		httpPost.setEntity(entity);
		
		//IdleConnectionEvictor connEvictor = new IdleConnectionEvictor(cm);
        //connEvictor.start();
        
		CloseableHttpResponse response = httpclient.execute(httpPost);
		String content = null;
		try {
			HttpEntity entity2 = response.getEntity();
			// do something useful with the response body
			content = getContent(response,encode);
			// and ensure it is fully consumed
			EntityUtils.consume(entity2);
		} finally {
			response.close();
		}
        
		//Thread.sleep(20000);
        //connEvictor.shutdown();
        //connEvictor.join();
        
        return content;
	}

	/**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
    
	/**
	 * 发送单个HTTP请求
	 * 
	 * @param url  请求地址
	 * @param json 请求数据JSON格式
	 * @throws Exception
	 */
	public static String doPost(String url, String json, String encode) throws Exception {
		CloseableHttpClient httpclient = bulidHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json;charset="+encode+"");

		StringEntity entity = new StringEntity(json,encode);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = httpclient.execute(httpPost);
		String content = null;
		try {
			HttpEntity entity2 = response.getEntity();
			// do something useful with the response body
			content = getContent(response,encode);
			// and ensure it is fully consumed
			EntityUtils.consume(entity2);
		} finally {
			response.close();
		}
		return content;
	}
	 
	
	/**
	 * 发送单个HTTP请求
	 * 
	 * @param url
	 *            请求地址
	 * @param json
	 *            请求数据JSON格式
	 * @throws Exception
	 */
	public static String doPost(String url, Map<String, String> params,String encode) throws Exception {
		Logger log = Logger.getLogger(HttpClientUtils.class);
		log.debug("HttpClient:DoPost::" + url);
		CloseableHttpClient httpclient = bulidHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvp = setParams(params);
		httpPost.setEntity(new UrlEncodedFormEntity(nvp, encode));
		CloseableHttpResponse response = httpclient.execute(httpPost);
		String content = null;
		
		try {
			HttpEntity entity2 = response.getEntity();
			content = getContent(response,encode);
			if(content.equals("") || content==null){
//				content = response.getHeaders("Location").toString();
				for(Header header : response.getHeaders("Location")) {
					content = header.getValue();
				}
			}
			EntityUtils.consume(entity2);
		} finally {
			response.close();
		}
		return content;
	}
	
	
	/**
	 * POST请求  包体传参
	 */
	public static String doPostp(String urlStr,String paramStr,String encode){
		try{  
            // Configure and open a connection to the site you will send the request  
            URL url = new URL(urlStr);  
            URLConnection urlConnection = url.openConnection();  
            // 设置doOutput属性为true表示将使用此urlConnection写入数据  
            urlConnection.setDoOutput(true);  
            // 定义待写入数据的内容类型，我们设置为application/x-www-form-urlencoded类型  
            urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset="+encode+"");  
            // 得到请求的输出流对象  
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());  
            // 把数据写入请求的Body  
            out.write(paramStr);  
            out.flush();  
            out.close();  
              
            // 从服务器读取响应  
              StringBuffer sb=new StringBuffer();
              String readLine=new String();
              BufferedReader responseReader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
              while((readLine=responseReader.readLine())!=null){
                sb.append(readLine).append("\n");
              }
              responseReader.close();
              System.out.println(sb.toString());
              return sb.toString();
        }catch(IOException e){  
            e.printStackTrace();
            return "error";
        }
	}
	

	/**
	 * <pre>
	 * 发送不带参数的GET的HTTP请求
	 * </pre>
	 * 
	 * @param reqUrl
	 *            HTTP请求URL
	 * @return HTTP响应的字符串
	 */
	public static String doGet(String reqUrl, String recvEncoding) {
		HttpURLConnection url_con = null;
		String responseContent = null;
		try {
			StringBuffer params = new StringBuffer();
			String queryUrl = reqUrl;
			int paramIndex = reqUrl.indexOf("?");

			if (paramIndex > 0) {
				queryUrl = reqUrl.substring(0, paramIndex);
				String parameters = reqUrl.substring(paramIndex + 1,
						reqUrl.length());
				String[] paramArray = parameters.split("&");
				for (int i = 0; i < paramArray.length; i++) {
					String string = paramArray[i];
					int index = string.indexOf("=");
					if (index > 0) {
						String parameter = string.substring(0, index);
						String value = string.substring(index + 1,
								string.length());
						params.append(parameter);
						params.append("=");
						params.append(URLEncoder.encode(value,
								requestEncoding));
						params.append("&");
					}
				}

				params = params.deleteCharAt(params.length() - 1);
			}

			URL url = new URL(queryUrl);
			url_con = (HttpURLConnection) url.openConnection();
			url_con.setRequestMethod("POST");
			System.setProperty("sun.net.client.defaultConnectTimeout",
					String.valueOf(connectTimeOut));// （单位：毫秒）jdk1.4换成这个,连接超时
			System.setProperty("sun.net.client.defaultReadTimeout",
					String.valueOf(readTimeOut)); // （单位：毫秒）jdk1.4换成这个,读操作超时
			
			url_con.setDoOutput(true);
			byte[] b = params.toString().getBytes();
			url_con.getOutputStream().write(b, 0, b.length);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			InputStream in = url_con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,
					recvEncoding));
			String tempLine = rd.readLine();
			StringBuffer temp = new StringBuffer();
			String crlf = System.getProperty("line.separator");
			while (tempLine != null) {
				temp.append(tempLine);
				temp.append(crlf);
				tempLine = rd.readLine();
			}
			responseContent = temp.toString();
			rd.close();
			in.close();
		} catch (IOException e) {
			log.error("网络故障", e);
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}

		return responseContent;
	}

	public static String doGet(String reqUrl) throws Exception {

		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;
		InputStreamReader inputStreamReader = null;
		try {
			URL url = new URL(reqUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setRequestMethod("GET");// 提交模式
//			httpUrlConn.connect();
			System.out.println(httpUrlConn.getResponseCode());
			if (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// 将返回的输入流转换成字符串
				inputStream = httpUrlConn.getInputStream();
			}
			else 
				inputStream = httpUrlConn.getErrorStream();
			inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bufferedReader) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (null != inputStreamReader) {
					inputStreamReader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (null != inputStream) {
					inputStream.close();
					inputStream = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			inputStream = null;
		}
		return buffer.toString();
	}

	
	/**
	 * 发送Get请求
	 * 
	 * @param url  请求地址
	 * @param json 请求数据JSON格式
	 * @return
	 * @throws Exception
	 */
	public static String doGet(String url,String platID, String platPwd,String encode) throws Exception {
		CloseableHttpClient httpclient = bulidHttpClient();
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("Content-Type", "application/json;charset="+encode+"");
		httpGet.setHeader("Authorization", "platformID=\""+platID+"\",password=\""+platPwd+"\"");
		
		CloseableHttpResponse response = httpclient.execute(httpGet);
		String content = null;

		try {
			HttpEntity entity = response.getEntity();
			content = getContent(response,encode);
			EntityUtils.consume(entity);
		} finally {
			response.close();
		}
		return content;
	}

	/**
	 * 获取返回数据
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private static String getContent(HttpResponse response,String encode) {
		HttpEntity entity = response.getEntity();
		byte[] bytes;
		String content = null;
		try {
			bytes = EntityUtils.toByteArray(entity);
			content = new String(bytes, encode);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	private static List<NameValuePair> setParams(Map<String, String> map) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (null != map) {
			Iterator<String> iter = map.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				String value = map.get(key);
				nvps.add(new BasicNameValuePair(key, value));
			}
		}
		return nvps;
	}
	

	/**
	 * 初始化HTTP连接池
	 * @return
	 */
	/*private static PoolingHttpClientConnectionManager initManger(){
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		
		//Increase max total connection to 200  
		cm.setMaxTotal(200);//总共保持200个连接(对于多个通过该http访问的网站)
		
		//Increase default max connection per route to 20  
		cm.setDefaultMaxPerRoute(10); //每个网站的默认连接最多20个
		
		//Increase max connections for localhost:80 to 50
		//连接池里面可以保持长连接到"http://open.wo.com.cn/"地址的最大数是50个,如果你请求"http://open.wo.com.cn/"这个地址的量很大，
		//把50个HTTP连接都占完了，那新的请求过来就需要等到其他使用连接池里面到这个地址的HTTP连接释放了才行
		HttpHost wo = new HttpHost("http://open.wo.com.cn/", 80);
		cm.setMaxPerRoute(new HttpRoute(wo), 80);
		
		HttpHost qq = new HttpHost("http://www.qq.com/",80);
		cm.setMaxPerRoute(new HttpRoute(qq), 50);
		
		
		
		return cm;
	}*/
	
	/**
	 * 获取HttpClient对象
	 * @param cm
	 * @return
	 */
	private static CloseableHttpClient bulidHttpClient(){
		//清除过期连接
		//IdleConnectionEvictor ice = IdleConnectionEvictor.getInstance(cm);
		//ice.closed();
		cm.closeExpiredConnections(); //清除过期链接
		//cm.closeIdleConnections(5, TimeUnit.SECONDS); //一段时间内不活动的连接
		
		CloseableHttpClient httpClient = HttpClients.custom()  
		        .setConnectionManager(cm)
		        .setDefaultRequestConfig(requestConfig)
		        .build();
		
		return httpClient;
	}
	
	/**
	 * 连接回收策略
	 * @author Jvi
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 *
	 */
	/*public static class IdleConnectionEvictor extends Thread {
        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionEvictor(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections(); //过期链接
                        // Optionally, close connections
                        // that have been idle longer than 5 sec
                        connMgr.closeIdleConnections(5, TimeUnit.SECONDS); //一段时间内不活动的连接
                    }
                }
            } catch (Exception ex) {
                // terminate
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

    }*/
	public static void downloadFile(String destFile,String url,Map<String, String> headerMap,String body,String encode) throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = bulidHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json;charset="+encode+"");
		if(headerMap != null){
			for(String key : headerMap.keySet()){
				String value = headerMap.get(key);
				httpPost.setHeader(key, value);
			}
		}
		StringEntity entity = new StringEntity(body, encode);
		httpPost.setEntity(entity);
        
		CloseableHttpResponse response = httpclient.execute(httpPost);
		try {
			InputStream is = response.getEntity().getContent();
			FileOutputStream bytestream = new FileOutputStream(destFile);
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			bytestream.close();
			bytestream.flush();
			// do something useful with the response body
			// and ensure it is fully consumed
			is.close();
		} finally {
			response.close();
		}
	}
	public static String uploadFile(String url, Map<String, String> headerMap,File file,String encode) {
		CloseableHttpClient httpclient = bulidHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json;charset="+encode+"");
		if(headerMap != null){
			for(String key : headerMap.keySet()){
				String value = headerMap.get(key);
				httpPost.setHeader(key, value);
			}
		}
		try {
			InputStreamEntity inputStreamEntity = new InputStreamEntity(new FileInputStream(file),file.length());
			httpPost.setEntity(inputStreamEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			String content = getContent(response,encode);
			EntityUtils.consume(entity);
			response.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return null;
    }
	public static String doPost(String url, Map<String, String> headerMap,String body,String encode) throws Exception {
		Logger log = Logger.getLogger(HttpClientUtils.class);
		log.debug("HttpClient:DoPost::" + url);
		CloseableHttpClient httpclient = bulidHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json;charset="+encode+"");
		if(headerMap != null){
			for(String key : headerMap.keySet()){
				String value = headerMap.get(key);
				httpPost.setHeader(key, value);
			}
		}
		StringEntity entity = new StringEntity(body,encode);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = httpclient.execute(httpPost);
		String content = null;
		
		try {
			HttpEntity entity2 = response.getEntity();
			content = getContent(response,encode);
			if(content.equals("") || content==null){
//				content = response.getHeaders("Location").toString();
				for(Header header : response.getHeaders("Location")) {
					content = header.getValue();
				}
			}
			EntityUtils.consume(entity2);
		} finally {
			response.close();
		}
		return content;
	}
	public static String doPost(String url,String json,String requestCode,String responseCode) throws Exception {
		CloseableHttpClient httpclient = bulidHttpClient();
		
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json;charset="+requestCode);
		StringEntity entity = new StringEntity(json);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = httpclient.execute(httpPost);
		String content = null;
		try {
			HttpEntity entity2 = response.getEntity();
			// do something useful with the response body
			content = getContent(response,responseCode);
			// and ensure it is fully consumed
			EntityUtils.consume(entity2);
		} finally {
			response.close();
		}
		return content;
	}
	public static void main(final String[] args) throws Exception {
		String url = "http://open.wo.com.cn/openapi/getchannelpaymentsms/v1.0";
		String json = "{\"appKey\":\"api_test05\",\"appName\":\"压力测试appName\",\"callbackData\":\"1\",\"callbackUrl\":\"http://58.240.19.130:18888/feeapi4/payment.do\",\"outTradeNo\":\"10011419821193432298986930164574489\",\"subject\":\"压力测试Subject\",\"timeStamp\":\"20141229104632\",\"totalFee\":0.05,\"signType\":\"HMAC-SHA1\",\"signature\":\"kazArOz0Z1+zgDbzoggtB8PdTHU=\"}";
		String platID = "8f0dcfc5-6548-445d-962f-0e266d3d1f63"; 
		String platPwd = "";
		String s = HttpClientUtils.doPost(url, json, platID, platPwd,HttpClientUtils.UTF8);
		System.out.println(s);
	}
}
