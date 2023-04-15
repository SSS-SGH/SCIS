<%-- 
Put here HTML to include at the end of the main page (index.jsp).
You can use CSS to put these elements in any part of the page.
--%>

<%@include file="../custom/view/stage.jsp"%>
<%@include file="../custom/view/introduction.jsp"%>
<script>
$("#stage_name").insertAfter("#application_name");
if ($("#sign_in_box").length) {
    $("#introduction").insertAfter("#sign_in_box");
} else {
    $("#introduction").remove();
}
</script>