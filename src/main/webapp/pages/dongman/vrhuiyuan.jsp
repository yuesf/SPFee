<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%String path = request.getContextPath();%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>订购</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<link rel="stylesheet" href="<%=path%>/pages/dongman/common/css/mui.min.css"/>
		<link rel="stylesheet" href="<%=path%>/pages/dongman/common/css/mui.css"/>
		<link rel="stylesheet" type="text/css" href="<%=path%>/pages/dongman/common/css/app.css"/>
		<script src="<%=path%>/pages/dongman/common/js/mui.js"></script>
		<script src="<%=path%>/pages/dongman/common/js/mui.min.js"></script>
	</head>
	<script type="text/javascript">
	
		function ready(){
		}
		function buyNormal(){
			buy("P00010");
		}
		function buyZunxiang()
		{
			buy("P01000");
		}
		function buy(payCode){
			mui.post('<%=path%>'+'/dongman/createorder',{payCode:payCode},function(data){
				//获得服务器响应
				location.href = data.url;
			},'json'
			);
		}
	</script>
	<style type="text/css">
		.mui-content-padded{
			  	margin: 10px;
			  }
		body{text-align:center;}
	</style>
	<body onload="ready()">
		<header id="header" class="mui-bar mui-bar-nav">
			<a class="mui-icon mui-icon-left-nav mui-pull-left" href="./index.html"></a>
			<h1 class="mui-title">会员购买</h1>
		</header>
		
		<div class="mui-content mui-content-padded">
			<br />
			<div align="center">
			<img alt="" src="<%=path%>/pages/dongman/common/img/logo.png"/>
			</div>
			温馨提示:体验会员需支付0.1元/月.尊享会员需支付10元/月。
			<br />
			<br />
			<div>
			<button onclick = "buyNormal()" type="button" class="mui-btn mui-btn-primary mui-btn-block" >体验会员</button>
			</div>
			<br />
			<div>
			<button onclick = "buyZunxiang()"type="button" class="mui-btn mui-btn-primary mui-btn-block" >尊享会员</button>
			</div>
			
		</div>
		
	</body>
</html>
