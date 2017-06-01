/**
 * 
 */
package com.qy.sp.fee.modules.piplecode.legao;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 * 02福富SDK代码渠道 乐高
 * @author yuesf 2017年4月20日
 */

@Controller
@RequestMapping(value = "/piple")
public class LegaoController {

	@Resource
	private LegaoService dsWoService;

	@RequestMapping(value = "/legao/sync", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String channelRequest(String channel_order_no, String pay_code, String status, String price,
			String create_time, String phone, String cpparam) {
		String result = "error";
		try {
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("channel_order_no", channel_order_no);
			bodyObject.put("pay_code", pay_code);
			bodyObject.put("status", status);
			bodyObject.put("price", price);
			bodyObject.put("status", status);
			bodyObject.put("create_time", create_time);
			bodyObject.put("phone", phone);
			bodyObject.put("cpparam", cpparam);
			JSONObject requestObject = JSONObject.fromObject(bodyObject);
			result = dsWoService.processPaySuccess(requestObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
