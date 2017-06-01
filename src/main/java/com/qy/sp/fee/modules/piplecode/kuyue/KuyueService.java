/**
 * 
 */
package com.qy.sp.fee.modules.piplecode.kuyue;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.NumberUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TOrderExtKey;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

/**
 * @author yuesf 2017年4月15日
 *
 */
//@Service
public class KuyueService extends ChannelService {
	private Logger log = Logger.getLogger(KuyueService.class);
	public final static String PAY_SUCCESS = "0000"; // 请求通道成功
	private final static String REQ_SUCCESS = "0";

	public String getPipleId() {
		return "14924845041064260113816";
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("KuyueService processGetSMS requestBody:" + requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String ipProvince = requestBody.optString("ipProvince");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		if (StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey) || StringUtil.isEmpty(pipleId)) {
			result.put("resultCode", GlobalConst.CheckResult.MUST_PARAM_ISNULL + "");
			result.put("resultMsg", GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		}

		//请求渠道
		BaseChannelRequest req = new BaseChannelRequest();
		req.setApiKey(apiKey);
		req.setImsi(imsi);
		req.setProductCode(productCode);
		req.setMobile(mobile);
		req.setIpProvince(ipProvince);
		// 调用合法性校验
		BaseResult bResult = this.accessVerify(req, pipleId);
		if (bResult != null) {// 返回不为空则校验不通过
			result.put("resultCode", bResult.getResultCode());
			result.put("resultMsg", bResult.getResultMsg());
			return result;
		}
		String groupId = KeyHelper.createKey();
		statistics(STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
		TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
		TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
		TPipleProductKey ppkey = new TPipleProductKey();
		ppkey.setPipleId(pipleId);
		ppkey.setProductId(tProduct.getProductId());
		TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
		TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
		KuyueOrder order = new KuyueOrder();
		order.setOrderId(KeyHelper.createKey());
		order.setGroupId(groupId);
		order.setPipleId(pipleId);
		order.setChannelId(tChannel.getChannelId());
		order.setMobile(mobile);
		order.setImsi(imsi);
		order.setImei(imei);
		order.setProductId(tProduct.getProductId());
		order.setOrderStatus(GlobalConst.OrderStatus.INIT);
		order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
		order.setCreateTime(DateTimeUtils.getCurrentTime());
		order.setAmount(new BigDecimal(tProduct.getPrice() / 100.0));
		order.setProvinceId(req.getProvinceId());
		order.setExtData(extData);
		if (requestBody.containsKey("fromType")) {
			if (GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)) {
				order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_SMS));
			} else if (GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)) {
				order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_SDK));
			} else if (GlobalConst.FromType.FROM_TYPE_API.equals(fromType)) {
				order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
			}
		} else {
			order.setFromType(NumberUtil.getInteger(GlobalConst.FromType.FROM_TYPE_API));
		}
		try {
			SaveOrderInsert(order);
			result.put("orderId", order.getOrderId());
			String param = pipleProduct.getPipleProductCode() + order.getOrderId();
			TreeMap<String, String> treeMap = new TreeMap<String, String>();
			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			treeMap.put("app_key", piple.getPipleAuthA());
			treeMap.put("billing_no", pipleProduct.getPipleProductCode());
			treeMap.put("timestamp", timestamp);
			treeMap.put("phone", order.getMobile());
			String sign = ParamsSign.value(treeMap, piple.getPipleAuthB());
			// 注意这个地方
			sign = URLEncoder.encode(sign, "UTF-8");
			//
			// 请求短信验证码地址
//			String reqUrl = piple.getPipleUrlA() + "?" + "app_key=" + piple.getPipleAuthA() + "&phone="
//					+ order.getMobile() + "&billing_no=" + pipleProduct.getPipleProductCode() + "&timestamp="
//					+ timestamp + "&reback_url=" + piple.getNotifyUrlA() + "&sign=" + sign;
//			statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
//			String pipleResult = HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);

			 String state="state";
			NameValuePair[] params = {
					new BasicNameValuePair("app_key",treeMap.get("app_key")),
					new BasicNameValuePair("phone",treeMap.get("phone")),
					new BasicNameValuePair("billing_no",treeMap.get("billing_no")),
					new BasicNameValuePair("state",state),
					new BasicNameValuePair("reback_url",piple.getNotifyUrlA()),
					new BasicNameValuePair("sign",sign),
					new BasicNameValuePair("timestamp",treeMap.get("timestamp"))};
			String pipleResult = HttpClientCommon.post(piple.getPipleUrlA(), params) ;
			if (pipleResult != null && !"".equals(pipleResult)) {
				System.out.println(pipleResult);
//				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId, pipleResult);
//				//
//				JSONObject object = JSONObject.fromObject(pipleResult);
//				String code = object.getString("res_code");
//				String msg = object.getString("res_message");
//				log.info("code:" + code + ",msg:" + msg);
//				String pipleOrderId = null;// 通道订单号
//				String actionTarget = null;// 验证接口
//				String authCode = null;// 验证码
//				String lsDongmanOrderId = null;// 外部orderId
//				if (REQ_SUCCESS.equals(code)) {
//					String trade_id = object.getString("trade_id");
//					String order_no = object.getString("order_no");
////					String state = object.getString("state");
//					log.info("trade_id:" + trade_id + ",order_no:" + order_no + ",state:" + state);
//					order.setResultCode(code);
//					// order.setSmsVertifyUrl(actionTarget);
//					order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
//					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
//					order.setResultCode(String.valueOf(code));
//					order.setPipleOrderId(pipleOrderId);
//					order.setOrderId(trade_id);
//					result.put("resultCode", GlobalConst.Result.SUCCESS);
//					result.put("resultMsg", "请求成功。");
//				} else {
//					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
//					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
//					order.setResultCode(code);
//					result.put("resultCode", GlobalConst.Result.ERROR);
//					result.put("resultMsg", "请求错误：" + msg);
//				}
			} else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
				result.put("resultCode", GlobalConst.Result.ERROR);
				result.put("resultMsg", "请求失败，接口异常");
			}
			SaveOrderUpdate(order);
			statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
			order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
			order.setModTime(DateTimeUtils.getCurrentTime());
			SaveOrderUpdate(order);
			result.put("resultCode", GlobalConst.Result.ERROR);
			result.put("resultMsg", "请求失败，接口异常:" + e.getMessage());
			return result;
		}
	}

	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("KuyueService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try{
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder==null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
				return result;
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
				return result;
			}else{
				statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
				TProduct tProduct = this.tProductDao.selectByPrimaryKey(tOrder.getProductId());
				KuyueOrder newOrder = new KuyueOrder();
				newOrder.setTOrder(tOrder);
				newOrder.setVerifyCode(verifyCode);
				newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
				TPipleProductKey ppkey = new TPipleProductKey();
				ppkey.setPipleId(getPipleId());
				ppkey.setProductId(tProduct.getProductId());
				TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
				TOrderExtKey oExtKey = new TOrderExtKey();
				oExtKey.setOrderId(orderId);
				oExtKey.setExtKey("smsVertifyUrl");
				TOrderExt urlExt = tOrderExtDao.selectByPrimaryKey(oExtKey);
				String param = pipleProduct.getPipleProductCode()+orderId; 
				String smsVertifyUrl = urlExt.getExtValue()+verifyCode;
				statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), smsVertifyUrl);
				String payResult= HttpClientUtils.doGet(smsVertifyUrl, HttpClientUtils.UTF8);
				log.info(" LSDongmanService getPageResult:"+ payResult);
				if(payResult != null && !"".equals(payResult)){
					statistics(STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), payResult);
					JSONObject object = JSONObject.fromObject(payResult);
					String code = object.getString("code");
					String msg = object.getString("msg");
					if(REQ_SUCCESS.equals(code)){
						result.put("resultCode", GlobalConst.Result.SUCCESS);
						result.put("resultMsg","请求成功。");
						newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
						SaveOrderUpdate(newOrder);
					}else{
						newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
						newOrder.setResultCode(code);
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败："+msg);
						SaveOrderUpdate(newOrder);
					}
					
				}else{
					newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
					newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
					newOrder.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
					SaveOrderUpdate(newOrder);
				}
				statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
				return result;
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
	}
	
	/**
	 * 回调结果处理
	 */
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {

		return super.processPaySuccess(requestBody);
	}
}
