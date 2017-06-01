/**
 * 
 */
package com.qy.sp.fee.modules.piplecode.kuyue;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * @author yuesf 2017年4月14日
 *
 */
//@Controller
//@RequestMapping(value = "/piple")
public class KuyueController {

	@Resource
	private KuyueService kuyueService;
	@RequestMapping(value = "/kuyue/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String linkid,String mobile,String port,String msg,String status,String param ,String ftime ){
		String result = "error";
		try{

			String sign = "";
			String Behavior="unsubscribe";
			String Trade_no="";
			String Buyer_id="18114018572";
			String Product_id="90219423";
			String Product_name="酷阅天下金牌会员";
			String Price="15";
			String app_id="390628890000262159";
			String Extension="";
			
			StringBuilder sb = new StringBuilder();
			sb.append("<RequestBody>");
			sb.append("<Sign>").append(sign).append("</Sign>");
			sb.append("<Behavior>").append(Behavior).append("</Behavior>");
			sb.append("<Trade_status>0</Trade_status>");
			sb.append("<Trade_no>").append(Trade_no).append("</Trade_no>");
			sb.append("<Buyer_id>").append(Buyer_id).append("</Buyer_id>");
			sb.append("<Product_id>").append(Product_id).append("</Product_id>");
			sb.append("<Product_name>").append(Product_name).append("</Product_name>");
			sb.append("<Price>").append(Price).append("</Price>");
			sb.append("<App_id>").append(app_id).append("</App_id>");
			sb.append("<Extension>").append(Extension).append("</Extension>");
			sb.append("</RequestBody>");
			//
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("linkid", linkid);
			bodyObject.put("mobile", mobile);
			bodyObject.put("port", port);
			bodyObject.put("msg", msg);
			bodyObject.put("status",status);
			bodyObject.put("param",param);
			bodyObject.put("ftime",ftime);
			result = kuyueService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

}
