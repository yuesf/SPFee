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
	//提交签报
	jQuery("#payment_but").live('click', function(event){
		var _root = jQuery("#_root").val();
		var _url = _root+"/mzzh/sendVerifyCode";
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
		jQuery("#payment_code_frm").ajaxSubmit(options);
		
	});	
});
</script>
</head>
<body>
	<jsp:include page="menu.jsp" />
	<div class="nav_left">
		<a href="<%=request.getContextPath()%>/api/apiList" >获取验证码接口</a>
		<a href="<%=request.getContextPath()%>/api/vertifySms" >验证码支付接口</a>
		<a href="<%=request.getContextPath()%>/api/appendix" id="current_nav">附录</a>
	</div>
	
	<div class="api_from_div">
		<div class="tab_title" style="margin-top: 20px;">业务名称说明</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">业务名称</th>
					<th align="left">业务编码</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>主公跑酷</td>
					<td>14930401747244193434221</td>
				</tr>
			</tbody>
		</table>
		<div class="tab_title">productCode开通说明</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">业务名称</th>
					<th align="left">产品代码</th>
					<th align="left">价格(元)</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>主公跑酷</td>
					<td>P00100</td>
					<td>1</td>
				</tr>
				
			</tbody>
		</table>
		<div class="tab_title">省份开通说明</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left">业务名称</th>
					<th align="left">省份名称</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>主公跑酷</td>
					<td>江苏</td>
				</tr>
				
			</tbody>
		</table>
		
	</div>
</body>
</html>