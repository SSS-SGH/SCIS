<%@ include file="imports.jsp"%>

<%@ page import="org.openxava.controller.meta.MetaAction" %>
<%@ page import="org.openxava.util.Is"%>
<%@ page import="com.openxava.phone.controller.PhoneManager" %>

<jsp:useBean id="context" class="org.openxava.controller.ModuleContext" scope="session"/>

<%
org.openxava.controller.ModuleManager manager = (org.openxava.controller.ModuleManager) context.get(request, "manager", "org.openxava.controller.ModuleManager");
manager.setSession(session);
String mode = request.getParameter("xava_mode"); 
if (mode == null) mode = manager.isSplitMode()?"detail":manager.getModeName();
PhoneManager phoneManager = new PhoneManager(manager);
%>

<div id="phone_dropdown_actions" class="phone-dropdown ox-button-bar"> 

<%
for (MetaAction action: manager.getMetaActions()) {
	if (phoneManager.showsAction(action, mode, request)) {
	%>
		<div onclick="phone.hideDropdown()">
			<xava:link action="<%=action.getQualifiedName()%>"/>
		</div>
	<%
	}
}
%>
</div>

