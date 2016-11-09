<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head></head>
	<body>
		<div style="border:1px solid;padding:5px;">
			<form id="myform">
				application_name :<input type="text" name="name" placeholder="请输入数字或者IP"/><br/><br/>
				server_port:<input type="text" name="s_port" placeholder="请输入四位数字"/><br/><br/>
				connector_port:<input type="text" name="c_port" placeholder="请输入四位数字"/><br/><br/>
				<input type="hidden" name="flag" value="false"/>
			</form>
			<input type="button" name="create" value="生成tomcat和重启脚本"/>
			<!-- <input type="button" name="download" value="下载tomcat和重启脚本" disabled/> -->
			<input type="button" name="upload" value="上传至服务器"/>
			<input type="button" name="start" value="启动服务"/>
		</div>
	</body>
	<script type="text/javascript" src="js/jquery.js"></script>
	<script type="text/javascript">
	$(function(){
		$("input[name=create]").click(function(){
			var name = $("input[name=name]").val();
			var s_port = $("input[name=s_port]").val();
			var c_port = $("input[name=c_port]").val();
			if(name != "" && s_port != null && c_port!= null){
				$("input[name=flag]").val(false);
				var result = ajax("/sftp/config/create.do");
				result = eval("(" + result + ")");
				console.info(result.flag);
				if(result.flag == "true"){
				}else{
					if(confirm(result.message+ ",确认是否继续进行？")){
						$("input[name=flag]").val(true);
						result = ajax("/sftp/config/create.do");
					}
				}
			}else{
				alert("信息输入不全");
			}
		});
		$("input[name=upload]").click(function(){
			var result = ajax("/sftp/config/upload.do");		
		});
		$("input[name=start]").click(function(){
			var result = ajax("/sftp/config/start.do");		
		});
	});
	
	function ajax(url){
		var result = "";
		$.ajax({
			type:"POST",
			data:$("#myform").serialize(),
			url:url,
			dataType:"JSON",
			async:false,
			success:function(data){
				result = data;
			}
		});
		return result;
	}
	</script>
</html>
