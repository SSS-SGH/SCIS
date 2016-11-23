<%@ include file="../imports.jsp"%>
<%@ page import="org.openxava.model.meta.MetaProperty" %>

<jsp:useBean id="style" class="org.openxava.web.style.Style" scope="request"/>

<%
String propertyKey = request.getParameter("propertyKey");
MetaProperty p = (MetaProperty) request.getAttribute(propertyKey);
Object value = (Boolean) request.getAttribute(propertyKey + ".value");
String fvalue = (String) request.getAttribute(propertyKey + ".fvalue");
String checked=Boolean.TRUE.equals(value)?"checked='true'":"";
boolean editable="true".equals(request.getParameter("editable"));
String disabled=editable?"":"disabled";
String script = request.getParameter("script");
String agent = (String) request.getAttribute("xava.portlet.user-agent");
if (null != agent && agent.indexOf("MSIE")>=0) {
    script = org.openxava.util.Strings.change(script, "onchange", "onclick");
}
String action = request.getParameter("xava_action");
if (action != null && action.startsWith("CRUD.search")) {
	String trueSelected = "true".equalsIgnoreCase(fvalue)?"selected":"";
	String falseSelected = "false".equalsIgnoreCase(fvalue)?"selected":"";
	String emptySelected =  (fvalue == null || "".equalsIgnoreCase(fvalue) || "null".equalsIgnoreCase(fvalue))?"selected":"";
%>
	<%-- inspired by comparatorsBooleanCombo.jsp --%>
	<select id="<%=propertyKey%>" name="<%=propertyKey%>" class=<%=style.getEditor()%> >
		<option value="null" <%=emptySelected%> ></option>
		<option value="true" <%=trueSelected%> >[x] <xava:message key="yes"/></option>
		<option value="false" <%=falseSelected%> >[&nbsp;] <xava:message key="no"/></option>
	</select>	

<% } else { %>

	<INPUT id="<%=propertyKey%>" type="checkbox" name="<%=propertyKey%>" class=<%=style.getEditor()%>
		tabindex="1" 
		value="true" 
		title="<%=p.getDescription(request)%>"	
		<%=checked%>
		<%=disabled%>
		<%=script%>
	/>
<% } %>

<% if (!editable) { %>
	<input type="hidden" name="<%=propertyKey%>" value="<%=value%>">
<% } %>			
