<%@ include file="../../xava/imports.jsp"%>

<div id="introduction">
    <div id="intro_welcome">
        <xava:message key="intro_welcome" />
    </div>
    <div id="intro_stage">
        <%
        String servletPathLower2 = request.getServletContext().getRealPath("").toLowerCase();
        %>
        <% if (servletPathLower2.contains("prod")) { %>
            <xava:message key="intro_stage_prod" />
        <% } else if (servletPathLower2.contains("test")) { %>
            <xava:message key="intro_stage_test" />
        <% } else { %>
            <xava:message key="intro_stage_dev" />
        <% } %>
    </div>
    <div id="intro_dedication">
        <xava:message key="intro_dedication" />
    </div>
</div>
