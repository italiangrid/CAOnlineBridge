<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>IGI CA online - Certificate Request</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/main.css' />">
	
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
	
	<script type="text/javascript">
		$(document).ready(function(){
			jQuery("#link").click(function(event) {
				event.preventDefault(); //to stop the default loading
				var a_href = $('#link').attr('href'); // getting the a href link
				jQuery("#overlay").css('display','block'); // displaying the overlay
				jQuery("#popup").css('display','block'); // displaying the popup
				jQuery("#popup").fadeIn(500); // Displaying popup with fade in animation
				setTimeout(function() {
					jQuery("#popup").fadeIn(4000); //function to redirect the page after few seconds
						window.location.replace("http://"+a_href); // the link
					}, 3000);
			}); 
			
			var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
			
			if(is_chrome){
				//$('.errorblock').show();
				//$('#contentLeft').hide();
			}
			
		});
		
		function loading(){
			jQuery("#overlay").css('display','block'); // displaying the overlay
			jQuery("#popup").css('display','block'); // displaying the popup
			jQuery("#popup").fadeIn(500); // Displaying popup with fade in animation
		}
		
	</script>

</head>
<body>

	<div id="page">

		<%@ include file="/WEB-INF/jsp/header.jsp"%>
		<h2>Request Certificate</h2>
		<div id="content">



			<div id="contentLeft">
				<c:set var="browser" value="${header['User-Agent']}" scope="session"/>
				<c:choose>
				<c:when test="${fn:contains(header['User-Agent'],'MSIE')}">
					<%@ include file="/WEB-INF/jsp/certReqIE.jsp"%>
				</c:when>
				<c:otherwise>
					<%@ include file="/WEB-INF/jsp/certReqFirefox.jsp"%>
				</c:otherwise>
				</c:choose>
			</div>
			
			<div class="errorblock" style="display:none;">
				Chrome isn't supported for this functionality.<br/>
				Try with Firefox or Internet Explorer.
			</div>

			
		</div>

		<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>

		<%@ include file="/WEB-INF/jsp/footer.jsp"%>
		
		

	</div>
	
	<div id="wrapper">
		<div id="overlay" style="display:none;"></div>
		<div  id="popup" style="display:none;">
			<img src="<c:url value='/images/loading.gif'/>" />
		</div>
	
	</div>
	
	
</body>
</html>
