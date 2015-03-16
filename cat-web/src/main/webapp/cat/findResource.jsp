<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="java.util.*,java.net.URL,org.xcom.cat.core.*"
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
    //
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);

    String resUri = request.getParameter("resourceUri");
    String nodeId = request.getParameter("nodeId");

    ClassLoader classLoader = null;
    if (nodeId != null) {
        nodeId = nodeId.trim();
        if (!"".equals(nodeId)) {
            CLNode node = ClassloaderAnalysisTool.getCLNode(nodeId);
            if (node != null) {
                classLoader = node.getClassLoader();
            }
        }
    }
    if (classLoader == null) {
        classLoader = Thread.currentThread().getContextClassLoader();
    }
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
label {
	color: red;
	font-weight: bold;
}

li {
	color: blue;
}
</style>
</head>
<body>
	<div>
		<h3>当前 ClassLoader</h3>
		<strong><%=classLoader.getClass().getName()%></strong><br>
		<%=classLoader.toString()%>
	</div>
	<div>
		<form method="post">
			<h3>请输入资源类路径 (例: java/lang/String.class )</h3>
			<input type="text" name="resourceUri" size="100"
				value="<%//
            if (resUri == null)
                out.print("java/lang/String.class");
            else
                out.print(resUri);%>" />
			<input type="submit" value="搜索" />
		</form>
	</div>
	<div>
		<h3>搜索结果</h3>
		<%
		    //搜索
		    if (resUri != null && !resUri.equals("")) {
		        resUri = resUri.trim();
		        URL url = classLoader.getResource(resUri);
		        if (url != null) {
		%>
		<ol>
			<%
			    //
			            Enumeration resources = classLoader.getResources(resUri);
			            Object value;
			            while (resources.hasMoreElements()) {
			                value = resources.nextElement();
			                out.print("<li>" + value);
			                if (url.equals(value)) {
			                    out.print("<label>有效</label>");
			                }
			                out.print("</li>");
			            }
			%>
		</ol>
		<%
		    //
		        } else {
		%>
		未找到<%=resUri%>
		<%
		    //
		        }
		    }
		%>
	</div>
</body>
</html>
