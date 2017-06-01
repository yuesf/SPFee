<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" /> 
<jsp:include page="base.jsp" />
<title>计费平台开发者中心</title>
<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery("#payment_but").live('click', function(event){
// 		var _url = "http://123.56.158.156:8318/spfee/channel/getSms";
		var _url = "http://127.0.0.1:8180/SPFee/channel/getSms";
		jQuery("#alert_message").html();
		
		var options = {
			url: _url,
	        success: function (data) {
	           if(null==data || ''==data){
	        	   jQuery("#alert_message").html("未查询到数据！");
	           }else{
	        	   var str = JSON.stringify(data); 
	        	   jQuery("#alert_message").html(str);
	           }
	           jQuery("#alert_message").show();
	        }
	    };
		jQuery("#get_code_frm").ajaxSubmit(options);
		
	});	
});
</script>
</head>
<body>
	<jsp:include page="menu.jsp" />
	
	<div class="nav_left">
		<a href="<%=request.getContextPath()%>/api/apiList" id="current_nav">获取验证码接口</a>
		<a href="<%=request.getContextPath()%>/api/vertifySms" >验证码支付接口</a>
		<a href="<%=request.getContextPath()%>/api/appendix" >附录</a>
	</div>
	
	<div class="api_from_div">
		<jsp:include page="getSmsBase.jsp" /> 
		
		<div id="alert_message"></div>
		<div class="tab_title">在线调试，输入参数后，请单击“调用”按钮。</div>
		
	<form id="get_code_frm" name="get_code_frm" method="post">
		<table class="tab2" cellpadding="0" border="0" cellspacing="0" >
			<thead>
				<tr>
					<th width="10%">参数</th>
					<th align="left">值</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td align="right"><font color="red">*</font>productCode：</td>
					<td><input type="text" id="productCode" name="productCode" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>apiKey：</td>
					<td><input type="text" id="apiKey" name="apiKey" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>apiPwd：</td>
					<td><input type="text" id="apiPwd" name="apiPwd" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>pipleId：</td>
					<td><input type="text" id="pipleId" name="pipleId" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>mobile：</td>
					<td><input type="text" id="mobile" name="mobile" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>imsi：</td>
					<td><input type="text" id="imsi" name="imsi" /></td>
				</tr>
				
				<tr>
					<td align="right"><font color="red">*</font>imei：</td>
					<td><input type="text" id="imei" name="imei" /></td>
				</tr>
				<tr>
					<td align="right"><font color="red">*</font>省份：</td>
					<td><input type="text" id="ipProvince" name="ipProvince" /></td>
				</tr>
				<tr>
					<td align="right">扩展数据：</td>
					<td><input type="text" id="extData" name="extData" /></td>
				</tr>
				
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="button" id="payment_but" name="payment_but" class="def_button" value="调用" />
					</td>
				</tr>
			</tfoot>
		</table>
	</form>
		
		<div class="tab_title">请求消息体参数</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">参数名</th>
					<th align="left">类型</th>
					<th align="left">选取原则</th>
					<th align="left">说明</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>productCode</td>
					<td>string</td>
					<td>必录</td>
					<td>产品代码</td>
				</tr>
				<tr>
					<td>apiKey</td>
					<td>string</td>
					<td>必录</td>
					<td>渠道代码</td>
				</tr>
				
				<tr>
					<td>apiPwd</td>
					<td>string</td>
					<td>必录</td>
					<td>渠道密码</td>
				</tr>
				
				<tr>
					<td>pipleId</td>
					<td>string</td>
					<td>必录</td>
					<td>业务Id （见附录）</td>
				</tr>
				<tr>
					<td>mobile</td>
					<td>string</td>
					<td>必录</td>
					<td>电话号码</td>
				</tr>
				<tr>
					<td>imsi</td>
					<td>string</td>
					<td>必录</td>
					<td>imsi</td>
				</tr>
				<tr>
					<td>imei</td>
					<td>string</td>
					<td>必录</td>
					<td>imei</td>
				</tr>
				<tr>
					<td>省份</td>
					<td>string</td>
					<td>必录</td>
					<td>给渠道开通的省份名称</td>
				</tr>
				<tr>
					<td>扩展数据</td>
					<td>string</td>
					<td>选填</td>
					<td>根据渠道回填信息</td>
				</tr>
				
			</tbody>
		</table>
		<div class="tab_title" style="margin-top: 20px;">响应消息体参数</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">参数名</th>
					<th align="left">类型</th>
					<th align="left">选取原则</th>
					<th align="left">说明</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>resultCode</td>
					<td>string</td>
					<td>必选</td>
					<td>0成功，非0失败。</td>
				</tr>
				
				<tr>
					<td>resultMsg</td>
					<td>string</td>
					<td>必选</td>
					<td>结果描述。</td>
				</tr>
				
				<tr>
					<td>orderId</td>
					<td>string</td>
					<td>必选</td>
					<td>订单号</td>
				</tr>
				
				
				<tr>
					<td>响应消息体格式：</td>
					<td colspan="3">
<pre>
{” resultCode”:”0”,” resultMsg”:”xxxx” , ”orderId”:”XXX”}
</pre>
					</td>
				</tr>
				
			</tbody>
		</table>
	</div>
</body>
</html>