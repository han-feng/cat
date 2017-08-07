<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page
	import="java.util.*,java.net.URL,org.xcom.cat.core.*,org.xcom.cat.core.ClassloaderAnalysisTool.*"
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
		<strong><%=classLoader.getClass().getName()%></strong>
		<pre><%=classLoader.toString()%></pre>
	</div>
	<div>
		<h3>重复资源</h3>

		<table border="1">
			<tr>
				<th>大小</th>
				<th>md5</th>
				<th></th>
				<th>位置</th>
			</tr>
			<%
			    List dupRes = ClassloaderAnalysisTool.findDupResouces(nodeId);
			    for (Iterator iter = dupRes.iterator(); iter.hasNext();) {
			        ResourceInfos infos = (ResourceInfos) iter.next();
			        String resName = infos.getName();
			        if (!infos.isSame()) {
			            resName = "<stong style='color:red'>" + resName
			                    + "</stong>";
			        }
			%>
			<tr>
				<th colspan="4"><%=resName%></th>
			</tr>
			<%
			    for (Iterator iter2 = infos.iterator(); iter2.hasNext();) {
			            ResourceInfo info = (ResourceInfo) iter2.next();
			            long size = info.getSize();
			            String md5 = info.getMd5();
			%>
			<tr>
				<td><%=size%></td>
				<td><%=md5%></td>
				<td>
					<%
					    if (info.isValid()) {
					%><label>有效</label> <%
     }
 %>
				</td>
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
