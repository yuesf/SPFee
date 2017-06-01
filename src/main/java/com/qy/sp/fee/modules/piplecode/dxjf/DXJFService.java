package com.qy.sp.fee.modules.piplecode.dxjf;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.MD5;
import com.qy.sp.fee.common.utils.MapCacheManager;
import com.qy.sp.fee.common.utils.MapUtil;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TPiple;
import com.qy.sp.fee.dto.TPipleProduct;
import com.qy.sp.fee.dto.TPipleProductKey;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.entity.BaseChannelRequest;
import com.qy.sp.fee.entity.BaseResult;
import com.qy.sp.fee.modules.piplecode.base.ChannelManager;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;
@Service
public class DXJFService extends ChannelService{
	public final static String RES_SUCCESS = "0";  // 请求通道成功
	public final static String PAY_SUCCESS = "0";  // 扣费成功
	public final static String PAY_TYPE = "ORDER";  // 扣费成功
	private  Logger log = Logger.getLogger(DXJFService.class);		

	@PostConstruct
	private void initConstruct() {
		// 电信
		ChannelManager.getInstance().putChannelService(getKey(), this);
	}

	private String getKey() {
		return "14955984421357213167808";
	}
	
	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		log.info("DXJFService processGetSMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String apiPwd = requestBody.optString("apiPwd");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
//		String imei = requestBody.optString("imei");
//		String iccid = requestBody.optString("iccid");
//		String ip = requestBody.optString("ip");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		if(StringUtil.isEmpty(fromType)){
			fromType = GlobalConst.FromType.FROM_TYPE_API;
		}
		if(StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey)  || StringUtil.isEmpty(mobile)){
			result.put("resultCode",GlobalConst.CheckResult.MUST_PARAM_ISNULL+"");
			result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		}else{
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setMobile(mobile);
			// 调用合法性校验
			BaseResult bResult = this.accessVerify(req,pipleId);
			if(bResult!=null){// 返回不为空则校验不通过
				result.put("resultCode",bResult.getResultCode());
				result.put("resultMsg",bResult.getResultMsg());
				return result;
			}
			String groupId = KeyHelper.createKey();
			statistics( STEP_GET_SMS_CHANNEL_TO_PLATFORM, groupId, requestBody.toString());
			TChannel tChannel = tChannelDao.selectByApiKey(req.getApiKey());
			TProduct tProduct = tProductDao.selectByCode(req.getProductCode());
			TPipleProductKey ppkey = new TPipleProductKey();
			ppkey.setPipleId(pipleId);
			ppkey.setProductId(tProduct.getProductId());
			TPipleProduct pipleProduct = tPipleProductDao.selectByPrimaryKey(ppkey);
			TPiple piple = tPipleDao.selectByPrimaryKey(pipleId);
			//保存订单
			TOrder order = new TOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setGroupId(groupId);
			order.setPipleId(pipleId);
			order.setChannelId(tChannel.getChannelId());
			order.setMobile(mobile);
			order.setImsi(imsi);
			order.setProductId(tProduct.getProductId());
			order.setOrderStatus(GlobalConst.OrderStatus.INIT);
			order.setSubStatus(GlobalConst.SubStatus.PAY_INIT);
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setAmount(new BigDecimal((tProduct.getPrice()==100?99:tProduct.getPrice())/100.0));  // 特殊处理 此代码P00100代表99积分即0.99元
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);
			order.setFromType(Integer.valueOf(fromType));
			try {
				SaveOrderInsert(order);
				result.put("orderId",order.getOrderId());
				//请求短信验证码
				Map<String, String> params = new HashMap<String, String>();
				params.put("cp_id", piple.getPipleAuthA());
				params.put("app_id", piple.getPipleAuthB());
				params.put("timestamp",DateTimeUtils.getFormatTime(DateTimeUtils.yyyyMMddHHmmssSSS));
//				params.put("timestamp",DateTimeUtils.yyyyMMddHHmmssSSS);
				params.put("product_id", pipleProduct.getPipleProductCode());
				params.put("user_id", order.getMobile());  // 待定
//				params.put("attach", order.getOrderId());
				params.put("attach", "");
				params.put("sign", getSign(params, piple.getPipleAuthC()));
//				String reqUrl = piple.getPipleUrlA()+"?"+"channel="+piple.getPipleAuthA()+"&imsi="+order.getImsi()+"&mobile="+order.getMobile()+"&param="+param;
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, piple.getPipleUrlA()+";"+params.toString());
//				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
//				String pipleResult= HttpClientUtils.doGet(reqUrl, HttpClientUtils.UTF8);
				String param = "app_id="+piple.getPipleAuthB()+"&attach=&cp_id="+piple.getPipleAuthA()+"&product_id="+pipleProduct.getPipleProductCode()
				+"&timestamp="+DateTimeUtils.getFormatTime(DateTimeUtils.yyyyMMddHHmmssSSS)+"&user_id="+order.getMobile()+"&sign="+getSign(params, piple.getPipleAuthC());
				String pipleResult= HttpClientUtils.sendPost(piple.getPipleUrlA(), param);
				log.info(param);
//				String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlA(), params, HttpClientUtils.UTF8);
				log.info(" DXJFService getSmsResult:"+  pipleResult+",orderId="+order.getOrderId());
//				{"res_code":"0","res_msg":"处理成功","extension":null,"data":{"trade_no":"20170105164142651400000003747182"}}
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId,pipleResult);
				if(pipleResult != null && !"".equals(pipleResult)){
					JSONObject jsonObj = JSONObject.fromObject(pipleResult);
					String res_code = null;
					String res_msg = null;
					String linkid = null;
					if(jsonObj.has("res_code") ){
						res_code = jsonObj.getString("res_code");
						res_msg = jsonObj.getString("res_msg");
					    String dataJson = jsonObj.getString("data");
						if(RES_SUCCESS.equals(res_code)){// 返回成功
							 JSONObject dataObj = JSONObject.fromObject(dataJson);
							 linkid = dataObj.getString("trade_no");
							 order.setPipleOrderId(linkid);
							 order.setResultCode(res_code);
							 order.setModTime(DateTimeUtils.getCurrentTime());
							 order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
							 order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
							 result.put("resultCode", GlobalConst.Result.SUCCESS);
							 result.put("resultMsg","请求成功。");
						}else{
							order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							order.setResultCode(res_code);
							order.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+res_msg);
						}
					}else{
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败:"+pipleResult);
					}
				}else{
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
					order.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode",GlobalConst.Result.ERROR);
					result.put("resultMsg","请求失败，接口异常");
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
				result.put("resultCode",GlobalConst.Result.ERROR);
				result.put("resultMsg","请求失败，接口异常:"+e.getMessage());
				return result;
			}
		}
	}
	
	
	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("DXJFService processVertifySMS requestBody:"+requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("vCode");
			result.put("orderId", orderId);
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if(tOrder==null){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_FAIL+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
				return result;
			}else if(tOrder.getOrderStatus()==GlobalConst.OrderStatus.SUCCESS && tOrder.getDecStatus()==GlobalConst.DEC_STATUS.UNDEDUCTED){
				result.put("resultCode",GlobalConst.CheckResult.ORDER_HASSUCCESS+"");
				result.put("resultMsg",GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
				return result;
			}else{
				statistics( STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
				TProduct tProduct = this.tProductDao.selectByPrimaryKey(tOrder.getProductId());
				BaseChannelRequest req = new BaseChannelRequest();
				req.setApiKey(apiKey);
				req.setMobile(tOrder.getMobile());
				req.setImsi(tOrder.getImsi());
				req.setProductCode(tProduct.getProductCode());
				BaseResult bResult = this.accessVerify(req, getKey());
				if(bResult!=null){// 返回不为空则校验不通过
					result.put("resultCode",bResult.getResultCode());
					result.put("resultMsg",bResult.getResultMsg());
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}else{
					
					TPiple piple = tPipleDao.selectByPrimaryKey(getKey());
					Map<String, String> params = new HashMap<String, String>();
					params.put("cp_id", piple.getPipleAuthA());
					params.put("app_id", piple.getPipleAuthB());
					params.put("timestamp",DateTimeUtils.getFormatTime(DateTimeUtils.yyyyMMddHHmmssSSS));
					params.put("trade_no", tOrder.getPipleOrderId());
					params.put("verify_code", verifyCode);
					params.put("sign", getSign(params, piple.getPipleAuthC()));
					statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), piple.getPipleUrlB()+";"+params.toString());
//					String confirmResult= HttpClientUtils.doGet(payUrl, HttpClientUtils.UTF8);
					String pipleResult= HttpClientUtils.doPost(piple.getPipleUrlB(), params, HttpClientUtils.UTF8);  // 返回支付确认地址
					log.info(" DXJFService payResult:"+  pipleResult+",orderId="+tOrder.getOrderId());
					statistics( STEP_BACK_VCODE_BASE_TO_PLATFORM, tOrder.getGroupId(), pipleResult);
					if(pipleResult != null && !"".equals(pipleResult)){
						JSONObject jsonObj = JSONObject.fromObject(pipleResult);
						String res_code = null;
						String res_msg = null;
						if(jsonObj.has("res_code") ){
							res_code = jsonObj.getString("res_code");
							res_msg = jsonObj.getString("res_msg");
							if(RES_SUCCESS.equals(res_code)){// 返回成功
								 tOrder.setResultCode(res_code);
								 tOrder.setModTime(DateTimeUtils.getCurrentTime());
								 tOrder.setOrderStatus(GlobalConst.OrderStatus.TRADING);
								 tOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
								 result.put("resultCode", GlobalConst.Result.SUCCESS);
								 result.put("resultMsg","请求成功。");
							}else{
								tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
								tOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
								tOrder.setResultCode(res_code);
								tOrder.setModTime(DateTimeUtils.getCurrentTime());
								result.put("resultCode", GlobalConst.Result.ERROR);
								result.put("resultMsg","请求失败:"+res_msg);
							}
						}else{
							tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
							tOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
							tOrder.setModTime(DateTimeUtils.getCurrentTime());
							result.put("resultCode", GlobalConst.Result.ERROR);
							result.put("resultMsg","请求失败:"+pipleResult);
						}
					}else{
						tOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						tOrder.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						tOrder.setModTime(DateTimeUtils.getCurrentTime());
						result.put("resultCode",GlobalConst.Result.ERROR);
						result.put("resultMsg","请求失败，接口异常");
					}
					SaveOrderUpdate(tOrder);
					statistics( STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
					return result;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			result.put("resultCode",GlobalConst.Result.ERROR);
			result.put("resultCode","服务器异常");
			return result;
		}
	}
	
	
	@Override
	public String getPipleKey() {
		return "PM1082";
	}
	
	/**
	 *	双短方式 
	 * */
	@Override
	public String processGetMessage(String mobile,String requestBody) throws Exception {
		String resultMsg = "";
		String args[] = requestBody.split("\\$");
		String apiKey = args[0];
		String productCode = args[1];
		String extData = null;
		if(args.length >2){
			extData = args[2];
		}
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getKey());
		request.put("productCode",productCode);
		request.put("mobile",mobile);
		request.put("extData",extData);
		JSONObject result = processGetSMS(request);
		if(result != null){
			if("0".equals(result.optString("resultCode"))){
				JSONObject param = new JSONObject();
				param.put("orderId",result.optString("orderId") );
				param.put("apiKey", apiKey);
				param.put("pipleKey", getPipleKey());
				param.put("productCode", productCode);
				MapCacheManager.getInstance().getSmsOrderCache().put(mobile,param.toString());
			}
			if(StringUtil.isNotEmptyString(result.optString("orderId"))){
				TOrder tOrder = tOrderDao.selectByPrimaryKey(result.optString("orderId"));
				if(tOrder != null){
					statistics(STEP_GET_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"1$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
				}
			}
			logger.debug(JSONObject.fromObject(result).toString());
		}
		return resultMsg;
	}
	
	@Override
	public String processSubmitMessage(String mobile,String requestBody) throws Exception {
		String jsonStr = MapCacheManager.getInstance().getSmsOrderCache().get(mobile);
		JSONObject param = JSONObject.fromObject(jsonStr);
		String orderId = param.optString("orderId");
		if(StringUtil.isEmpty(orderId)){
			return "";
		}
		TOrder tOrder = tOrderDao.selectByPrimaryKey(orderId);
		if(tOrder == null)
			return "";
		statistics(STEP_SUBMIT_MESSAGE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody);
		String args[] = requestBody.split("\\$");
		if(args.length <2)
			return "";
		String apiKey = args[0];
		String vcode = args[1];
		TChannel channel = tChannelDao.selectByApiKey(apiKey);
		if(channel == null)
			return "";
		JSONObject request = new JSONObject();
		request.put("apiKey",channel.getApiKey());
		request.put("apiPwd",channel.getApiPwd());
		request.put("pipleId",getKey());
		request.put("orderId",tOrder.getOrderId());
		request.put("vCode",vcode);
		JSONObject result = processVertifySMS(request);
		MapCacheManager.getInstance().getSmsOrderCache().remove(mobile);
		if(result != null){
			logger.debug(JSONObject.fromObject(result).toString());
			statistics(STEP_SUBMIT_MESSAGE_PLATFORM_TO_CHANNEL_RESULT, tOrder.getGroupId(),mobile+";"+"2$"+getPipleKey()+"$"+requestBody+";"+JSONObject.fromObject(result).toString());
		}
		return "";
	}
	
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("DXJFService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
//		bodyObject.put("cp_id", cp_id);
//		bodyObject.put("app_id", app_id);
//		bodyObject.put("timestamp", timestamp);
//		bodyObject.put("sign", sign);
//		bodyObject.put("trade_no",trade_no);
//		bodyObject.put("op_time",op_time);
//		bodyObject.put("op_type",op_type);
//		bodyObject.put("status",status);
//		bodyObject.put("product_id",product_id);
//		bodyObject.put("product_name",product_name);
//		bodyObject.put("price",price);
//		bodyObject.put("user_id",user_id);
//		bodyObject.put("attach",attach);
		String trade_no = requestBody.optString("trade_no");
		String op_type   = requestBody.optString("op_type");
		String status = requestBody.optString("status");
		TOrder order = tOrderDao.selectByPipleOrderId(trade_no);
		if(order!=null && order.getOrderStatus()!=GlobalConst.OrderStatus.SUCCESS){ // 订单未同步过，成功同步去重处理
			statistics(STEP_PAY_BASE_TO_PLATFORM, order.getGroupId(), requestBody.toString());
			TChannelPipleKey pkey = new TChannelPipleKey();
			pkey.setChannelId(order.getChannelId());
			pkey.setPipleId(order.getPipleId());
			TChannelPiple cp =  tChannelPipleDao.selectByPrimaryKey(pkey);
			if(cp == null){
				return "channel error";
			}
			boolean bDeducted = false; // 扣量标识
			if(PAY_SUCCESS.equals(status) && PAY_TYPE.equals(op_type)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt());  
			}else{
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
			}
			SaveOrderUpdate(order);
			if(!bDeducted){ // 不扣量 通知渠道
				notifyChannelAll(cp.getNotifyUrl(), order, null);
			}
		}
		JSONObject rst = new JSONObject();
		rst.put("res_code", "0");
		rst.put("res_msg", "处理成功");
		return rst.toString();
	}
	
	@Override
	protected boolean isUseableTradeDayAndMonth() {
		return true;
	}
	
	public String getSign(Map<String,String> map,String signKey){
		String signStr = null;
		String signContext = "";
		Map<String,String>  newMap= MapUtil.sortMapByKey(map);
		for(Map.Entry<String, String> entry:newMap.entrySet()){
			if(!StringUtil.isEmpty(entry.getValue()))
			signContext =signContext+ entry.getKey()+"="+entry.getValue()+"&";
//			System.out.println("signContext："+signContext);
		}   
		signContext = signContext+"key="+signKey;
		System.out.println("allsignContext："+signContext);
		signStr = MD5.getMD5(signContext).toUpperCase();
		return signStr;
	}
	
	public static void main(String[] args) {
		String json = "{\"res_code\":\"0\",\"res_msg\":\"处理成功\",\"extension\":null,\"data\":{\"trade_no\":\"20170105172312191400000003667961\"}}";
		JSONObject jsonObj = JSONObject.fromObject(json);
		String dataJson = jsonObj.getString("data");
			 JSONObject dataObj = JSONObject.fromObject(dataJson);
			 String linkid = dataObj.getString("trade_no");
			 System.out.println(linkid);
	}
	
}
