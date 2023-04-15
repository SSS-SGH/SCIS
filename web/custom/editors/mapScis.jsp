<%@ include file="../../xava/imports.jsp"%>

<%@ page import="java.util.List"%>
<%@ page import="java.util.Locale"%>
<%@ page import="org.openxava.controller.ModuleManager"%>
<%@ page import="org.openxava.tab.Tab"%>
<%@ page import="org.openxava.view.View"%>
<%@ page import="org.openxava.util.XavaResources"%>
<%@ page import="org.openxava.util.XavaPreferences"%>
<%@ page import="org.openxava.util.Is" %>
<%@ page import="org.openxava.web.Ids" %>
<%@ page import="ch.speleo.scis.ui.editors.MapScisEditorHelper"%>
<%@ page import="ch.speleo.scis.ui.editors.MapScisEditorHelper.ItemWithCoordinates"%>

<jsp:useBean id="context" class="org.openxava.controller.ModuleContext" scope="session"/>
<jsp:useBean id="errors" class="org.openxava.util.Messages" scope="request"/>
<jsp:useBean id="style" class="org.openxava.web.style.Style" scope="request"/>

<%
String viewObject = request.getParameter("viewObject");
String actionArgv = viewObject != null && !viewObject.equals("")?",viewObject=" + viewObject:"";
viewObject = (viewObject == null || viewObject.equals(""))?"xava_view":viewObject; 
org.openxava.view.View view = (org.openxava.view.View) context.get(request, viewObject);
String tabObject = request.getParameter("tabObject");
tabObject = (tabObject == null || tabObject.equals(""))?"xava_tab":tabObject;
Tab tab = (Tab) context.get(request, tabObject);
ModuleManager manager = (ModuleManager) context.get(request, "manager", "org.openxava.controller.ModuleManager");
%>


<% /* ***** the paging, copied from OpenXava's listEditor.jsp ***** */ %>

<%
String collection = request.getParameter("collection"); 
String id = "list";
String collectionArgv = "";
String prefix = "";
//String tabObject = request.getParameter("tabObject");
String scrollId = "list_scroll"; 
//tabObject = (tabObject == null || tabObject.equals(""))?"xava_tab":tabObject;
if (collection != null && !collection.equals("")) {
	id = collection;
	collectionArgv=",collection="+collection;
	prefix = tabObject + "_";
	scrollId = "collection_scroll"; 
}
boolean sortable = !Is.emptyString(collection) && view.isRepresentsSortableCollection();  
boolean simple = sortable;
String groupBy = tab.getGroupBy();
boolean grouping = !Is.emptyString(groupBy);
int totalSize = -1; 
if (!tab.isRowsHidden()) {
	totalSize = totalSize < 0?tab.getTotalSize():totalSize; 
}
%>

<% if (!tab.isRowsHidden() && !simple) { %>
<table width="100%" class="<%=style.getListInfo()%>" cellspacing=0 cellpadding=0>
<tr class='<%=style.getListInfoDetail()%>'>
<td class='<%=style.getListInfoDetail()%>'>
<%
int last=tab.getLastPage();
int current=tab.getPage();
if (current > 1) {
%>
<span class='<%=style.getFirst()%>'><span class='<%=style.getPageNavigationArrow()%>' <%=style.getPreviousPageNavigationEvents(Ids.decorate(request, id))%>><xava:image action='List.goPreviousPage' argv='<%=collectionArgv%>'/></span></span>
<%
}
else {
%>
<span class='<%=style.getFirst()%>'><span class='<%=style.getPageNavigationArrowDisable()%>'>
<i class="mdi mdi-menu-left"></i>
</span></span>
<%	
} 
%>
<span class="<%=style.getPageNavigationPages()%>">
<%
for (int i=1; i<=last; i++) {
if (i == current) {
	if (style.isShowPageNumber()) {  
%>
<span class="<%=style.getPageNavigationSelected()%>"><%=i%></span>
	<% } else {%>
<span class="<%=style.getPageNavigationSelected()%>">
	<img 
		src='<%=request.getContextPath()%>/<%=style.getImagesFolder()%>/<%=style.getPageNavigationSelectedImage()%>' 
		border=0 align="absmiddle"/>
</span>	
	<% } %>
<% } else { 
		if (style.isShowPageNumber()) { 
%>
<xava:link action='List.goPage' argv='<%="page=" + i + collectionArgv%>' cssClass="<%=style.getPageNavigation()%>"><%=i%></xava:link>
<% 
		} else {
%>
<span class="<%=style.getPageNavigation()%>">
	<img 
		src='<%=request.getContextPath()%>/<%=style.getImagesFolder()%>/<%=style.getPageNavigationImage()%>' 
		border=0 align="absmiddle"/>
</span>
<%				
		}
	}
} 
%>
</span>
<%
if (!tab.isLastPage()) {
%>
<span class='<%=style.getLast()%>'>
<span class='<%=style.getPageNavigationArrow()%>' <%=style.getNextPageNavigationEvents(Ids.decorate(request, id)) %>>
<xava:image action='List.goNextPage' argv='<%=collectionArgv%>'/>
</span>
</span>
<% 
} 
else {
%>
<span class='<%=style.getLast()%>'>
<span class='<%=style.getPageNavigationArrowDisable()%>'>
<i class="mdi mdi-menu-right"></i>
</span>
</span>
<%	
} 
%>
<% if (style.isChangingPageRowCountAllowed()) { %>
&nbsp;
<select id="<xava:id name='<%=id + "_rowCount"%>'/>" class=<%=style.getEditor()%>
	onchange="openxava.setPageRowCount('<%=request.getParameter("application")%>', '<%=request.getParameter("module")%>', '<%=collection==null?"":collection%>', this)">
	<% 
	int [] rowCounts = { 5, 10, 12, 15, 20, 50, 100 }; // The peformance with more than 50 rows is poor for page reloading
	for (int i=0; i<rowCounts.length; i++) {
		String selected = rowCounts[i] == tab.getPageRowCount()?"selected='selected'":""; 	
	%>	
	<option value="<%=rowCounts[i]%>" <%=selected %>><%=rowCounts[i]%></option>
	<%
	}
	%>
</select>
<span class="<%=style.getRowsPerPage()%>">	 
<xava:message key="rows_per_page"/>
</span>
<% } // of if (style.isChangingPageRowCountAllowed()) %>
</td>
<td style='text-align: right; vertical-align: middle' class='<%=style.getListInfoDetail()%>'>
<% if (XavaPreferences.getInstance().isShowCountInList() && !style.isShowRowCountOnTop() && !grouping && totalSize < Integer.MAX_VALUE) { %> 
<xava:message key="list_count" intParam="<%=totalSize%>"/>
<% } %>
<%-- if (collection == null && style.isHideRowsAllowed() && !grouping && totalSize < Integer.MAX_VALUE) { %> 
(<xava:link action="List.hideRows" argv="<%=collectionArgv%>"/>)
<% } --%>
<% if (last != 1 /* only one page */) { %> 
/ <xava:link action="CollectionScis.loadAllData" argv="<%=collectionArgv%>"/>
<% } %>
</td>
</tr>
</table>
<% } %>


<% /* ***** the map ***** */ %>

<div id="scis_map" style="height: 80ex; overflow: hidden;"></div>

<%
MapScisEditorHelper helper = new MapScisEditorHelper(tab, view, request, errors);
%>

<%--
 Leaflet is already bound in OpenXava, otherwise add its CSS and JavaScript.
 Use Leaflet.TileLayer.Swiss (hosted on GitHub) to support LV95 coordinates and simplify SwissTopo integration.
 Otherwise proj4js can do the transformation of coordinates in JavaScript.
 Generally, script here are not working because loaded through AJAX. Do externalise the scripts. 
 <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.2/dist/leaflet.css"
     integrity="sha256-sA+zWATbFveLLNqWO2gtiw3HL/lh1giY/Inf1BJ0z14="
     crossorigin=""/>
 <!-- Make sure you put this AFTER Leaflet's CSS -->
 <script src="https://unpkg.com/leaflet@1.9.2/dist/leaflet.js"
     integrity="sha256-o9N1jGDZrf5tS+Ft4gbIK7mYMipq9lqpVJ91xHSyKhg="
     crossorigin=""></script>
 <script src="https://cdn.jsdelivr.net/npm/leaflet-tilelayer-swiss@2.3.0/dist/Leaflet.TileLayer.Swiss.umd.js" crossorigin
     integrity="sha384-M4p8VfZ8RG6qNiPYA3vOCApQXAlLtnJXVPdydMYPAsvvIDsWp2dqqzF2OEeWWNhy"
     id="leaflet-tilelayer-swiss" onload="console.log('leaflet-tilelayer-swiss loaded')"></script>
 <script src="https://cdnjs.cloudflare.com/ajax/libs/proj4js/2.8.0/proj4.js" 
     integrity="sha512-ha3Is9IgbEyIInSb+4S6IlEwpimz00N5J/dVLQFKhePkZ/HywIbxLeEu5w+hRjVBpbujTogNyT311tluwemy9w==" 
     crossorigin="anonymous" referrerpolicy="no-referrer"></script>
--%>

 <script type="text/javascript">
 
    function initMap() {

    	<%
        boolean coordsSystemKnown = true;
    	int coordsSystemEpsgNr = helper.getCoordsSystemEpsgNr();
        if (coordsSystemEpsgNr == 3857) {
        %>
	    	var map = L.map('scis_map').setView([46.8712, 8.2388], 8);
		    var layerMapColor = L.tileLayer('https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-farbe/default/current/3857/{z}/{x}/{y}.jpeg', {
		    	minZoom: 14, maxZoom: 27,
		        attribution: '&copy; <a href="https://www.swisstopo.admin.ch/">swisstopo</a>'
		    }).addTo(map);
		    var layerMapGrey = L.tileLayer('https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-grau/default/current/3857/{z}/{x}/{y}.jpeg', {
		    	minZoom: 14, maxZoom: 27,
		        attribution: '&copy; <a href="https://www.swisstopo.admin.ch/">swisstopo</a>'
		    });
		    var layerSatellite = L.tileLayer('https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.swissimage/default/current/3857/{z}/{x}/{y}.jpeg', {
		    	minZoom: 14, maxZoom: 28,
		        attribution: '&copy; <a href="https://www.swisstopo.admin.ch/">swisstopo</a>'
		    });
    	<%
        } else if (coordsSystemEpsgNr == 2056) {
        %>
		    var map = L.map('scis_map', {
		        crs: L.CRS.EPSG2056
		    })
		    var layerMapColor = L.tileLayer.swiss().addTo(map);
		    var layerMapGrey = L.tileLayer.swiss({layer: 'ch.swisstopo.pixelkarte-grau'});
		    var layerSatellite = L.tileLayer.swiss({layer: 'ch.swisstopo.swissimage', maxNativeZoom: 28
		    });
		    map.fitSwitzerland();
	    <%
        } else if (coordsSystemEpsgNr == -1) {
        	String textMissingMapAnnotation= helper.sanitizeForJavascript(XavaResources.getString("missing_map_annotation"));
        %>
	    	$('#scis_map').append('<div class="error"><%=textMissingMapAnnotation%></div>');
	    	return null;
    	<%
	    	coordsSystemKnown = false;
        } else {
        %>
        	$('#scis_map').append('<div class="error">Unknown coordinate system <%=coordsSystemEpsgNr%>.</div>');
        	return null;
       	<%
    	    coordsSystemKnown = false;
        }
        if (coordsSystemKnown) {
        %>
		    //L.control.zoom({position: 'topright'}).addTo(map);
		    var baseMaps = {
		    	'<xava:label key="mapColored" />': layerMapColor,
		    	'<xava:label key="mapGrey" />': layerMapGrey,
		    	'<xava:label key="mapAerialImage" />': layerSatellite
		    };
		    L.control.layers(baseMaps, {}).addTo(map);
		    map.zoomControl.setPosition('topright');
		    L.control.scale({ position: 'bottomright' }).addTo(map);
	    	/*L.marker(L.CRS.EPSG2056.unproject(L.point(2_661_010, 1_191_440))).bindPopup('the center').addTo(map);*/
	    	return map;
       	<%
	    }
	    %>
    }

    function fillMap(map) {
    <%
        if (helper.isViewableOnMap()) {
        	String textDebug = XavaResources.getString("missing_coord_column", "blabla");
        	int finalIndex = simple?Integer.MAX_VALUE:tab.getFinalIndex();
        	Iterable<ItemWithCoordinates> iterable = 
        			(request.getParameter("action.CollectionScis.loadAllData") != null) && (tab.getLastPage() == 1)
        			? helper.iterateOverAll() 
    				: helper.iterateFromTo(tab.getInitialIndex(), finalIndex);
        	for (ItemWithCoordinates item: iterable) {
	            if (item.getCoordEast() == null || item.getCoordNorth() == null) {
	            	continue;
	            }
	        	StringBuilder text = new StringBuilder();
	            for (int f = 0 ; f < item.getFields().size() ; f++) {
            		text
            			.append(helper.sanitizeForJavascript( helper.getLabelOfField(f) ))
            			.append(": ")
            			.append(helper.sanitizeForJavascript( item.getFields().get(f) ))
            			.append("<br/>");
	            }
	            String action=request.getParameter("rowAction");
	            action=action==null?manager.getEnvironment().getValue("XAVA_LIST_ACTION"):action;
	        	StringBuilder actionCode = new StringBuilder()
	        			.append("if (!getSelection().toString()) ")
	        			.append("openxava.executeAction( ")
       					.append("\\'").append(request.getParameter("application")).append("\\', ")
       					.append("\\'").append(request.getParameter("module")).append("\\', ")
        				.append("false, false, ")
        				.append("\\'").append(action).append("\\', ")
        				.append("\\'").append("row=").append(helper.getCurrentRow()).append("\\' ")
        				.append(");")
	        			;
   %>
                var point = L.point(<%=item.getCoordEast()%>, <%=item.getCoordNorth()%>);
	            L.marker(L.CRS.EPSG<%=coordsSystemEpsgNr%>.unproject(point))
	             .bindPopup('<div class="<%=item.getCssStyle()%>" onclick="<%=actionCode%>"><%=text.toString()%></div>')
	             .addTo(map);
    <%
	        }
        }
    %>
    }
    var map = initMap();
    if (map != null) {
        fillMap(map);
    }
 </script>
<%
%>
 