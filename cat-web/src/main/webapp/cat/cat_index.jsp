<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="java.util.*,java.lang.reflect.*,org.xcom.cat.core.*"
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
    // 注意：部分jsp容器不支持泛型语法
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);

			ClassloaderAnalysisTool.process(this.getClass().getClassLoader(),
					"JSP.class");
			ClassloaderAnalysisTool.process("JSP.serviceThread");
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
		out.print("><span class=\"title\">");
		out.print(node.getType());
		out.print("</span>&nbsp;" + Arrays.toString(node.getTags())
				+ "&nbsp;<a href=\"javascript:displayNode('node_urls_"
				+ node.getId() + "')\">详细信息</a>");
		out.print("&nbsp;<a href=\"javascript:findResource('" + node.getId()
				+ "')\">查找资源</a>");
		out.print("&nbsp;<a href=\"javascript:findDupResource('" + node.getId()
				+ "')\">探测重复资源</a>");
		out.println("<div id=\"node_urls_" + node.getId()
				+ "\" style=\"display: none;\">" + node.getDescription());
		out.println("<ol>");
		String[] urls = node.getClasspath();
		int urlsize = urls.length;
		for (int i = 0; i < urlsize; i++) {
			out.println("<li>" + urls[i] + "</li>");
		}
		out.print("</ol>");
		out.println("</div>");
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
	font-weight: normal;
}

td {
	padding: 20px 10px 0 0;
	vertical-align: top;
}

.title {
	font-weight: bold;
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
	function findResource(nodeId) {
		window.open("findResource.jsp?nodeId=" + nodeId);
	}
	function findDupResource(nodeId) {
		window.open("findDupResource.jsp?nodeId=" + nodeId);
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
	说明：CAT-Core 标记 CAT 工具类所在的类加载器；JSP.class 标记 JSP
	类定义所在的类加载器；JSP.serviceThread 标记 JSP 服务线程所使用的上下文类加载器。
</body>
</html>
