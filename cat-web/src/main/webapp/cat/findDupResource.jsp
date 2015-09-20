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
		<h3>重复资源</h3>
		<%
		    Map dupRes = ClassloaderAnalysisTool.findDupResouces(nodeId);
		    for (Iterator iter = dupRes.entrySet().iterator(); iter.hasNext();) {
		        Map.Entry entry = (Map.Entry) iter.next();
		%><table>
			<tr>
				<th colspan="2"><%=entry.getKey()%></th>
			</tr>
			<%
			    List list = (List) entry.getValue();
			        for (int i = 0; i < list.size(); i++) {
			%><tr>
				<td>&nbsp;</td>
				<td><%=list.get(i)%></td>
			</tr>
			<%
			    }
			%>
		</table>
		<%
		    }
		%>

	</div>
</body>
</html>
