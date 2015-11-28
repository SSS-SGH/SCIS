<%@page import="org.openxava.util.Is"%>
<%@page import="org.openxava.web.Ids"%>
<%@page import="org.openxava.util.Labels"%>
<%@page import="java.util.Collection"%>
<%@page import="org.openxava.controller.meta.MetaAction"%>
<%@page import="org.openxava.controller.meta.MetaController"%>
<%@page import="org.openxava.controller.meta.MetaControllers"%>
<%@page import="org.openxava.controller.meta.MetaSubcontroller"%>

<%@ include file="imports.jsp"%>

<jsp:useBean id="style" class="org.openxava.web.style.Style" scope="request"/>
<jsp:useBean id="context" class="org.openxava.controller.ModuleContext" scope="session"/>

<%
org.openxava.controller.ModuleManager manager = (org.openxava.controller.ModuleManager) context.get(request, "manager", "org.openxava.controller.ModuleManager");
manager.setSession(session);
String controllerName = request.getParameter("controller");
String image = request.getParameter("image");
String id = Ids.decorate(request, "sc-" + controllerName);
String containerId = Ids.decorate(request, "sc-container-" + controllerName);
String buttonId = Ids.decorate(request, "sc-button-" + controllerName);
String imageId = Ids.decorate(request, "sc-image-" + controllerName);
String aId = Ids.decorate(request, "sc-a-" + controllerName);
String spanId = Ids.decorate(request, "sc-span-" + controllerName);
%>
<span id='<%=containerId%>'>
	<span id='<%=buttonId%>' class="<%=style.getButtonBarButton()%>">
		<a 
			id ='<%=aId%>'
			href="javascript:openxava.subcontroller('<%=id%>','<%=containerId%>','<%=buttonId%>','<%=imageId%>','<%=aId%>','<%=spanId%>')" 
			>
			<span
				id='<%=spanId%>' 
				style="padding:4px; background: url(<%=request.getContextPath()%>/<%=style.getImagesFolder()%>/<%=image%>) no-repeat 5px 50%;">				
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;		
			</span>
			<%= Labels.get(controllerName)%>
			<img  
				id='<%=imageId%>' 
				src='<%=request.getContextPath()%>/<%=style.getImagesFolder()%>/ascending3.gif'/>
			&nbsp;
		</a>
	</span>
	
	<div id="<%=id%>" class="<%=style.getSubcontroller()%>" style="display:none;">
		<table>
		<%
		MetaController controller = MetaControllers.getMetaController(controllerName);
		Collection actions = controller.getMetaActions();
		String mode = request.getParameter("xava_mode"); 
		if (mode == null) mode = manager.isSplitMode()?"detail":manager.getModeName();
		java.util.Iterator actionsIt = actions.iterator();
		while(actionsIt.hasNext()){
			MetaAction action = (MetaAction)actionsIt.next();
			if (action.appliesToMode(mode)) {
		%>	
			<tr><td>
				<jsp:include page="barButton.jsp">
					<jsp:param name="action" value="<%=action.getQualifiedName()%>"/>
					<jsp:param name="addSpaceWithoutImage" value="true"/>
				</jsp:include>
			</td></tr>
		<%
			}
		}
		%>
		</table>
	</div>
</span>	