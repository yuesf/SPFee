package com.qy.sp.fee.modules.piplecode.dxjf;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/piple")
public class DXJFController {

	@Resource
	private DXJFService dxjfService;
	@RequestMapping(value = "/dxjf/sync" ,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String channelRequest(String cp_id,String app_id,String timestamp,String sign,String trade_no,String op_time ,
			String op_type,String status, String product_id,String product_name,String price,String user_id,String attach){
		String result = "error";
		try{
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("cp_id", cp_id);
			bodyObject.put("app_id", app_id);
			bodyObject.put("timestamp", timestamp);
			bodyObject.put("sign", sign);
			bodyObject.put("trade_no",trade_no);
			bodyObject.put("op_time",op_time);
			bodyObject.put("op_type",op_type);
			bodyObject.put("status",status);
			bodyObject.put("product_id",product_id);
			bodyObject.put("product_name",product_name);
			bodyObject.put("price",price);
			bodyObject.put("user_id",user_id);
			bodyObject.put("attach",attach);
			result = dxjfService.processPaySuccess(bodyObject);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
