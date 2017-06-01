package com.qy.sp.fee.modules.piplecode.kuduan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.qy.sp.fee.common.utils.DateTimeUtils;
import com.qy.sp.fee.common.utils.GlobalConst;
import com.qy.sp.fee.common.utils.KeyHelper;
import com.qy.sp.fee.common.utils.StringUtil;
import com.qy.sp.fee.dto.TChannel;
import com.qy.sp.fee.dto.TChannelPiple;
import com.qy.sp.fee.dto.TChannelPipleKey;
import com.qy.sp.fee.dto.TOrder;
import com.qy.sp.fee.dto.TOrderExt;
import com.qy.sp.fee.dto.TProduct;
import com.qy.sp.fee.modules.piplecode.base.ChannelManager;
import com.qy.sp.fee.modules.piplecode.base.ChannelService;

import net.sf.json.JSONObject;

@Service
public class KuduanService extends ChannelService {
	public final static String PORT = "1069009216288";

	private Logger log = Logger.getLogger(KuduanService.class);
	public final static String PAY_SUCCESS = "DELIVRD"; // 请求通道成功
	private final static String REQ_SUCCESS = "0";

	@PostConstruct
	private void initConstruct() {
		// 电信
		ChannelManager.getInstance().putChannelService(getKey(), this);
	}

	private String getKey() {
		return "14937735114799979922779";
	}
	@Override
	public String processPaySuccess(JSONObject requestBody) throws Exception {
		logger.info("KuduanService 支付同步数据:" + requestBody);
		String error = "error";
		if (requestBody == null || "".equals(requestBody) || "{}".equals(requestBody.toString())) {
			return error;
		}
		String groupId = KeyHelper.createID();
		statistics(STEP_PAY_BASE_TO_PLATFORM, groupId, requestBody.toString());
		String spnumber = requestBody.optString("spnumber");
		String msg = requestBody.optString("msg");
		String mobile = requestBody.optString("mobile");
		String fee = requestBody.optString("fee");
		String link_id = requestBody.optString("link_id");
		String status = requestBody.optString("status");
		String paytime = requestBody.optString("paytime");
		TOrder nOrder = tOrderDao.selectByPipleOrderId(link_id);
		// 未同步过
		if(nOrder==null){
			List<TChannelPiple> cPiples = tChannelPipleDao.getListByPipleId(getKey());
			TProduct product = tProductDao.selectByPrice(Integer.parseInt(fee));
			TChannelPiple cPiple= null;
			for (TChannelPiple tChannelPiple : cPiples) {
				String productIds = tChannelPiple.getProductIds() == null ? "":tChannelPiple.getProductIds();
				String productId = fee+";";
				if (productIds.indexOf(productId) >= 0 && !tChannelPiple.getChannelId().equals("1003")) {
					cPiple = tChannelPiple;
					break;
				}
			}
			String channelId ="1003";
			if(cPiple!=null){
				channelId = cPiple.getChannelId();
			}
			TChannel channel = tChannelDao.selectByPrimaryKey(channelId);
			
			KuduanOrder order = new KuduanOrder();
			order.setOrderId(KeyHelper.createKey());
			order.setCreateTime(DateTimeUtils.getCurrentTime());
			order.setGroupId(groupId);
			order.setMobile(mobile);
			order.setPipleId(getKey());
			order.setChannelId(channel.getChannelId());
			order.setProductId(product.getProductId());
			order.setPipleOrderId(link_id);
			order.setExtData(paytime);
			if(!StringUtil.isEmpty(mobile)){
				order.setProvinceId(getProvinceIdByMobile(mobile, false));
			}
			order.setAmount(new BigDecimal(product.getProductId()));
			order.setSpnumber(spnumber);
			order.setMsg(msg);
			
			TChannelPipleKey key = new TChannelPipleKey();
			key.setChannelId(channel.getChannelId());
			key.setPipleId(getKey());
			TChannelPiple cp = tChannelPipleDao.selectByPrimaryKey(key);
			boolean isSend = false;
			// 扣费成功
			if(PAY_SUCCESS.equals(status)){
				order.setOrderStatus(GlobalConst.OrderStatus.SUCCESS);
				order.setSubStatus(GlobalConst.SubStatus.PAY_SUCCESS);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setCompleteTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
				doWhenPaySuccess(order);
				boolean bDeducted  = order.deduct(cp.getVolt());
				if(!bDeducted){ 
					isSend =true;
				}
				if(isSend && !channel.getChannelId().equals("1003")){ // 不扣量 通知渠道
					notifyChannelSMS(cp.getNotifyUrl(), order, spnumber, PAY_SUCCESS);
				}
			}else {
				order.setOrderStatus(GlobalConst.OrderStatus.FAIL);
				order.setSubStatus(GlobalConst.SubStatus.PAY_ERROR);
				order.setModTime(DateTimeUtils.getCurrentTime());
				order.setResultCode(status);
			}
			SaveOrderInsert(order);
			return "OK";
		}else{
			return "order Synchronized";
		}
	}
	
	public class KuduanOrder extends TOrder{
		private String msg;  // 指令
		private String spnumber;  // 端口号
		
		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getSpnumber() {
			return spnumber;
		}

		public void setSpnumber(String spnumber) {
			this.spnumber = spnumber;
		}

		public List<TOrderExt> gettOrderExts() {
			List<TOrderExt> tOrderExts = new ArrayList<TOrderExt>();
			if(this.msg != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("msg");
				oExt.setExtValue(this.msg);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			if(this.spnumber != null){
				TOrderExt oExt = new TOrderExt();
				oExt.setExtKey("spnumber");
				oExt.setExtValue(this.spnumber);
				oExt.setOrderId(this.getOrderId());
				tOrderExts.add(oExt);
			}
			return tOrderExts;
		}
	}
}
