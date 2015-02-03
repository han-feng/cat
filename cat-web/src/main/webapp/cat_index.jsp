<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="java.lang.reflect.Method"%>
<%@page import="java.util.*,org.xcom.cat.core.*" language="java"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 注意：部分jsp容器不支持泛型语法
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			ClassloaderAnalysisTool.process(this.getClass().getClassLoader());
			ClassloaderAnalysisTool.process(Thread.currentThread()
					.getContextClassLoader());
			Collection roots = ClassloaderAnalysisTool.getRoots();
%>
<%!//
	private static void printClassLoaderNodes(CLNode node, JspWriter out)
			throws Exception {
		CLNode[] children = node.getChildren();
		int size = children.length;

		out.println("<table>");
		out.print("<tr><th");
		if (size > 1)
			out.print(" colspan='" + size + "'");
		out.print(">");
		out.print(node.getType());
		String[] urls = node.getClasspath();
		int urlsize = urls.length;
		out.print(" &nbsp; <a href=\"javascript:displayNode('node_urls_"
				+ node.getId() + "')\">详细信息</a>");
		out.println("<ol id='node_urls_" + node.getId()
				+ "' style='display: none;'>");
		for (int i = 0; i < urlsize; i++) {
			out.println("<li>" + urls[i] + "</li>");
		}
		out.print("</ol>");
		out.println("</th></tr>");

		if (size > 0) {
			out.print("<tr>");
			for (int i = 0; i < size; i++) {
				out.println("<td>");
				printClassLoaderNodes(children[i], out);
				out.println("</td>");
			}
			out.println("</tr>");
		}
		out.println("</table>");
	}%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
li {
	color: blue;
	font-weight: normal;
}

table {
	border-collapse: collapse;
	border: 0px solid black;
	margin: 0;
}

th {
	text-align: left;
	border: 1px solid black;
	padding: 5px;
}

td {
	padding: 20px 5px;
}
</style>
<script type="text/javascript">
	function displayNode(nodeId) {
		var node = document.getElementById(nodeId);
		if (node.style.display == "none")
			node.style.display = "block";
		else
			node.style.display = "none";
	}
</script>
</head>
<body>
	<%
	    for (Iterator itr = roots.iterator(); itr.hasNext();) {
	        printClassLoaderNodes((CLNode) itr.next(), out);
	%><hr>
	<%
	    }
	%>
</body>
</html>
