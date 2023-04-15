<%@ include file="../../xava/imports.jsp"%>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Locale"%>
<%@ page import="org.apache.commons.lang.ObjectUtils"%>
<%@ page import="org.openxava.util.Labels"%>
<%@ page import="ch.speleo.scis.model.karst.SpeleoObject"%>
<%@ page import="ch.speleo.scis.business.Podium"%>

		<%
		Podium podiumService = new Podium();
		Locale locale = new Locale("de", "CH");
		int nbCaves = 20;
		int i=0;
		String styleRaw = "border-bottom: 1px solid; padding-top: 3px; padding-bottom: 3px;";
		%>

	<h2><xava:message key="deepest_caves" /></h2>
	<table class="podium ox-list">
		<tr class="results-header portlet-section-header ox-list-header">
			<th align="right"><xava:message key="place" /></th>
			<th align="left" ><xava:label key="name" /></th>
			<th align="right"><xava:label key="depthAndElevation" /></th>
			<th align="right"><xava:label key="systemNr" /></th>
		</tr>
		<%
		List<SpeleoObject> deepestCaves = podiumService.getDeepestCaves(nbCaves);
		i=0;
		for (SpeleoObject cave: deepestCaves) {
			String styleClass = "results-row " + ((0==i%2)?"portlet-section-body":"portlet-section-alternate alt");
			i++;
		%>
		<tr class="<%= styleClass %>">
			<td align="right"><%= i %></td>
			<td align="left" ><%= ObjectUtils.toString(cave.getName(), "") %></td>
			<td align="right"><%= String.format(locale, "%,d", cave.getDepthAndElevationComputed()) %></td>
			<td align="right"><%= ObjectUtils.toString(cave.getSystemNr(), "") %></td>
		</tr>
		<%
		}
		%>
	</table>

	<h2><xava:message key="longest_caves" /></h2>
	<table class="podium ox-list">
		<tr class="results-header portlet-section-header ox-list-header">
			<th align="right"><xava:message key="place" /></th>
			<th align="left" ><xava:label key="name" /></th>
			<th align="right"><xava:label key="length" /></th>
			<th align="right"><xava:label key="systemNr" /></th>
		</tr>
		<%
		List<SpeleoObject> longestCaves = podiumService.getLongestCaves(nbCaves);
		i=0;
		for (SpeleoObject cave: longestCaves) {
			String styleClass = "results-row " + ((0==i%2)?"portlet-section-body":"portlet-section-alternate alt");
			i++;
		%>
		<tr class="<%= styleClass %>" style="border-bottom: 1px solid;">
			<td align="right"><%= i %></td>
			<td align="left" ><%= ObjectUtils.toString(cave.getName(), "") %></td>
			<td align="right"><%= String.format(locale, "%,d", cave.getLength()) %></td>
			<td align="right"><%= ObjectUtils.toString(cave.getSystemNr(), "") %></td>
		</tr>
		<%
		}
		%>
	</table>

