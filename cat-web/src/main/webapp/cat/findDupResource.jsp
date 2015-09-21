<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="org.xcom.cat.core.ClassloaderAnalysisTool.ResourceInfo"%>
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

table {
	border-collapse: collapse;
	border: 1px solid #ccc;
}

th, td {
	padding: 5px;
	text-align: left;
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
		<h3>重复资源</h3>

		<table border="1">
			<tr>
				<th>大小</th>
				<th>md5</th>
				<th>位置</th>
			</tr>
			<%
			    Map dupRes = ClassloaderAnalysisTool.findDupResouces(nodeId);
			    for (Iterator iter = dupRes.entrySet().iterator(); iter.hasNext();) {
			        Map.Entry entry = (Map.Entry) iter.next();
			%>
			<tr>
				<th colspan="3"><%=entry.getKey()%></th>
			</tr>
			<%
			    List list = (List) entry.getValue();
			        ResourceInfo info = (ResourceInfo) list.get(0);
			        long s0 = info.getSize();
			        String m0 = info.getMd5();
			        for (int i = 0; i < list.size(); i++) {
			            info = (ResourceInfo) list.get(i);
			            long size = info.getSize();
			            String md5 = info.getMd5();
			            if (size != s0 || !m0.equals(md5)) {
			%><tr style="color: red">
				<%
				    } else {
				%>
			
			<tr>
				<%
				    }
				%>
				<td><%=size%></td>
				<td><%=md5%></td>
				<td><%=info.getParent()%></td>
			</tr>
			<%
			    }
			    }
			%>
		</table>
	</div>
</body>
</html>
