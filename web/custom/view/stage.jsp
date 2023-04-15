
	<%
	String servletPathLower = request.getServletContext().getRealPath("").toLowerCase();
	String stage = servletPathLower.contains("prod") ? "prod" : servletPathLower.contains("test") ? "test" : "dev";
	%>
	<div id="stage_name" class="<%=stage%>"><%=stage%></div>
