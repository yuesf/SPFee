/**
 * 
 */
package com.qy.sp.fee.modules.piplecode.kuduan;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

/**
 *03酷阅天下短代能力
 * @author yuesf 2017年5月3日
 */
@Controller
@RequestMapping(value = "/piple")
public class KudanController {

	@Resource
	private KuduanService dsWoService;

	@RequestMapping(value = "/kuduan/sync", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String channelRequest(String spnumber, String msg, String mobile, String fee,
			String link_id, String status, String paytime) {
		String result = "error";
		try {
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("spnumber", spnumber);
			bodyObject.put("msg", msg);
			bodyObject.put("mobile", mobile);
			bodyObject.put("fee", fee);
			bodyObject.put("link_id", link_id);
			bodyObject.put("status", status);
			bodyObject.put("paytime", paytime);
			JSONObject requestObject = JSONObject.fromObject(bodyObject);
			result = dsWoService.processPaySuccess(requestObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


}
