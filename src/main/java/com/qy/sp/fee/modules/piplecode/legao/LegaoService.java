package com.qy.sp.fee.modules.piplecode.legao;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.HttpClientUtils;
import com.qy.sp.fee.common.utils.KeyHelper;
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

import net.sf.json.JSONObject;

/**
 * 
 * @author yuesf 2017年4月20日
 */
@Service
public class LegaoService extends ChannelService {
	public final static String PORT = "1069009216288";

	private Logger log = Logger.getLogger(LegaoService.class);
	public final static String PAY_SUCCESS = "0000"; // 请求通道成功
	private final static String REQ_SUCCESS = "0";
	
	@PostConstruct
	private void initConstruct() {
		//电信
		ChannelManager.getInstance().putChannelService("14930401747244193434221", this);
		//联通
		ChannelManager.getInstance().putChannelService("14932063591761975987400", this);
	}

	@Override
	public JSONObject processGetSMS(JSONObject requestBody) throws Exception {
		JSONObject result = new JSONObject();
		String productCode = requestBody.optString("productCode");
		String apiKey = requestBody.optString("apiKey");
		String mobile = requestBody.optString("mobile");
		String pipleId = requestBody.optString("pipleId");
		String imsi = requestBody.optString("imsi");
		String imei = requestBody.optString("imei");
		String extData = requestBody.optString("extData");
		String fromType = requestBody.optString("fromType");
		String ipProvince = requestBody.optString("ipProvince");
		if (StringUtil.isEmptyString(productCode) || StringUtil.isEmptyString(apiKey) || StringUtil.isEmpty(pipleId)) {
			result.put("resultCode", GlobalConst.CheckResult.MUST_PARAM_ISNULL + "");
			result.put("resultMsg", GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.MUST_PARAM_ISNULL));
			return result;
		} else {
			BaseChannelRequest req = new BaseChannelRequest();
			req.setApiKey(apiKey);
			req.setImsi(imsi);
			req.setProductCode(productCode);
			req.setMobile(mobile);
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
			int provinceId = 0;
			if (StringUtil.isEmpty(ipProvince)) {
				provinceId = mobileSegmentService.getProvinceIdByMobile(mobile);
			} else {
				provinceId = mobileSegmentService.getProvinceByIpProvince(ipProvince);
			}
			// 保存订单
			LegaoOrder order = new LegaoOrder();
			order.setProvinceId(provinceId);
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
			Date CurrentTime = DateTimeUtils.getCurrentTime();
			order.setCreateTime(CurrentTime);
			order.setAmount(new BigDecimal(tProduct.getPrice() / 100.0));
			order.setProvinceId(req.getProvinceId());
			order.setExtData(extData);

			if (requestBody.containsKey("fromType")) {
				if (GlobalConst.FromType.FROM_TYPE_SMS.equals(fromType)) {
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_SMS));
				} else if (GlobalConst.FromType.FROM_TYPE_SDK.equals(fromType)) {
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_SDK));
				} else if (GlobalConst.FromType.FROM_TYPE_API.equals(fromType)) {
					order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_API));
				}
			} else {
				order.setFromType(Integer.valueOf(GlobalConst.FromType.FROM_TYPE_API));
			}
			try {
				SaveOrderInsert(order);
				result.put("orderId", order.getOrderId());
				String reqUrl = piple.getPipleUrlA();
				String pay_code = pipleProduct.getPipleProductCode();
				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				timestamp = URLEncoder.encode(timestamp, "utf-8");
				reqUrl = reqUrl + "?pay_code=" + pay_code + "&phone=" + mobile + "&imei=" + imei + "&imsi=" + imsi
						+ "&create_time=" + timestamp + "&channelid=100080";
				statistics(STEP_GET_SMS_PLATFORM_TO_BASE, groupId, reqUrl);
				String pipleResult = HttpClientUtils.doGet(reqUrl);
				log.info(" DSWOService getPageResult:" + pipleResult + ",orderId=" + order.getOrderId());
				statistics(STEP_BACK_SMS_BASE_TO_PLATFORM, groupId, pipleResult);
				if (pipleResult != null && !"".equals(pipleResult)) {
					JSONObject object = JSONObject.fromObject(pipleResult);
					String code = object.getString("res_code");
					String msg = object.getString("msg");
					String channel_order_no = object.getString("channel_order_no");
					log.info("code:" + code + ",msg:" + msg + ",channel_order_no:" + channel_order_no);
					if (REQ_SUCCESS.equals(code)) {
						order.setResultCode(code);
						order.setOrderStatus(GlobalConst.OrderStatus.TRADING);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_SUCCESS);
						order.setPipleOrderId(channel_order_no);
						result.put("resultCode", GlobalConst.Result.SUCCESS);
						result.put("resultMsg", "请求成功。");
					} else {
						order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						order.setSubStatus(GlobalConst.SubStatus.PAY_GET_SMS_FAIL);
						order.setResultCode("1");
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg", "请求错误：" + msg);
					}
				} else {
					order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					order.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
					result.put("resultCode", GlobalConst.Result.ERROR);
					result.put("resultMsg", "请求失败，接口异常");
				}
				SaveOrderUpdate(order);
				statistics(STEP_BACK_SMS_PLATFORM_TO_CHANNEL, groupId, result.toString());
				return result;
			} catch (Exception ex) {
				ex.printStackTrace();
				result.put("resultCode", GlobalConst.Result.ERROR);
				result.put("resultCode", "服务器异常");
				return result;
			}
		}
	}

	@Override
	public JSONObject processVertifySMS(JSONObject requestBody) {
		log.info("LegaoService processVertifySMS requestBody:" + requestBody);
		JSONObject result = new JSONObject();
		try {
			String apiKey = requestBody.optString("apiKey");
			String orderId = requestBody.optString("orderId");
			String verifyCode = requestBody.optString("verifyCode");
			String productname = "主公跑酷";//requestBody.optString("productname");
			String contentname = "道具名称";//requestBody.optString("contentname");
			TOrder tOrder = this.tOrderDao.selectByPrimaryKey(orderId);
			if (tOrder == null) {
				result.put("resultCode", GlobalConst.CheckResult.ORDER_FAIL + "");
				result.put("resultMsg", GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_FAIL));
				return result;
			} else if (tOrder.getOrderStatus() == GlobalConst.OrderStatus.SUCCESS) {
				result.put("resultCode", GlobalConst.CheckResult.ORDER_HASSUCCESS + "");
				result.put("resultMsg",
						GlobalConst.CheckResultDesc.message.get(GlobalConst.CheckResult.ORDER_HASSUCCESS));
				return result;
			} else {
				statistics(STEP_SUBMIT_VCODE_CHANNEL_TO_PLATFORM, tOrder.getGroupId(), requestBody.toString());
				TProduct tProduct = this.tProductDao.selectByPrimaryKey(tOrder.getProductId());
				LegaoOrder newOrder = new LegaoOrder();
				newOrder.setTOrder(tOrder);
				newOrder.setVerifyCode(verifyCode);
				newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
				TPipleProductKey ppkey = new TPipleProductKey();
				ppkey.setPipleId(tOrder.getPipleId());
				ppkey.setProductId(tProduct.getProductId());
				TPiple piple = tPipleDao.selectByPrimaryKey(tOrder.getPipleId());
				String payUrl = piple.getPipleUrlB()+"?channel_order_no="+tOrder.getPipleOrderId()+"&verifycode="+verifyCode+"&productname="+productname+"&contentname="+contentname;
				statistics(STEP_SUBMIT_VCODE_PLARFORM_TO_BASE, tOrder.getGroupId(), payUrl);
				String payResult = HttpClientUtils.doGet(payUrl);
				if (payResult != null && !"".equals(payResult)) {
					JSONObject object = JSONObject.fromObject(payResult);
					String code = object.getString("res_code");
					String msg = object.getString("msg");
					if (REQ_SUCCESS.equals(code)) {
						result.put("resultCode", GlobalConst.Result.SUCCESS);
						result.put("resultMsg", "请求成功。");
						newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
					} else {
						newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
						newOrder.setResultCode("1");
						newOrder.setModTime(DateTimeUtils.getCurrentTime());
						newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
						newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
						result.put("resultCode", GlobalConst.Result.ERROR);
						result.put("resultMsg", "请求失败：" + msg);
						SaveOrderUpdate(newOrder);
					}
				} else {
					newOrder.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
					newOrder.setOrderStatus(GlobalConst.OrderStatus.FAIL);
					newOrder.setSubStatus(GlobalConst.SubStatus.PAY_SUBMIT_CODE_FAIL);
					newOrder.setModTime(DateTimeUtils.getCurrentTime());
					result.put("resultCode", GlobalConst.Result.ERROR);
					result.put("resultMsg", "请求失败，接口异常");
					SaveOrderUpdate(newOrder);
				}
				statistics(STEP_BACK_VCODE_PLATFORM_TO_CHANNEL, tOrder.getGroupId(), result.toString());
				return result;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("resultCode", GlobalConst.Result.ERROR);
			result.put("resultCode", "服务器异常");
			return result;
		}
	}
	
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("LegaoService 支付同步数据:"+requestBody);
		String error = "error";
		if(requestBody==null || "".equals(requestBody) || "{}".equals(requestBody.toString())){
			return error;
		}
		String status = requestBody.optString("status");
		String orderId = requestBody.optString("channel_order_no");
		String pay_code = requestBody.optString("pay_code");
		String price = requestBody.optString("price");
		String create_time = requestBody.optString("create_time");
		String phone = requestBody.optString("phone");
		String cpparam = requestBody.optString("cpparam");
		TOrder order = tOrderDao.selectByPipleOrderId(orderId);
		order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_INIT);
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
			if("0".equals(status)){
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_SUCCESS);
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				doWhenPaySuccess(order);
				bDeducted  = order.deduct(cp.getVolt()); 
				if(!bDeducted){ // 不扣量 通知渠道
					if(GlobalConst.FromType.FROM_TYPE_SMS.equals(order.getFromType())){
						notifyChannelSMS(cp.getNotifyUrl(), order,PORT, "ok");
					}else{
						notifyChannelAPI(cp.getNotifyUrl(), order, "ok");
					}
				}
			}else{
				order.setSyncResultCode(GlobalConst.SyncResultType.SYNC_ERROR);
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
			}
			SaveOrderUpdate(order);
		}
		return "success";
	}
}
