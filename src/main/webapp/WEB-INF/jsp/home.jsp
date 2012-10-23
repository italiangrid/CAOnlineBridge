<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
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
		<div id="content">
			<h2>Request Certificate</h2>
			<div id="content">



				<div id="contentLeft">

					<c:url var="saveUrl" value="/home/certReq" />
					<form:form modelAttribute="certificateRequest" method="POST"
						action="${saveUrl }">
						
						<strong>Personal Informations:</strong>
						<div id="reset"></div>
						<div id="personalInfo">
							<table>
								<tr>
									<td><form:label path="mail">Mail: ${certificateRequest.mail }</form:label></td>
									<td><form:hidden path="mail" /></td>
								</tr>

								<tr>
									<td><form:label path="cn">CN:  ${certificateRequest.cn }</form:label></td>
									<td><form:hidden path="cn" /></td>
								</tr>

								<tr>
									<td><form:label path="o">O:  ${certificateRequest.o }</form:label></td>
									<td><form:hidden path="o" /></td>
								</tr>

								<tr>
									<td><form:label path="l">L:  ${certificateRequest.l }</form:label></td>
									<td><form:hidden path="l" /></td>
								</tr>
								<tr>
								<td><form:label path="l">C: IT</form:label></td>
								<td></td>
							</tr>
							</table>
						</div>
						<!-- <div id="contetRight">Your personal data retreived from your
							Identity Provider.</div> -->
						<div id="reset"></div>

						<strong>Proxy password:</strong>
						<div id="reset"></div>
						<div id="password">
							<form:errors path="*" cssClass="errorblock" element="div" />
							<table>
								<tr>
									<td><form:label path="proxyPass1">Proxy Password:</form:label></td>
									<td><form:password path="proxyPass1" /></td>
								</tr>

								<tr>
									<td><form:label path="proxyPass2">Retype Password:</form:label></td>
									<td><form:password path="proxyPass2" /></td>
								</tr>

							</table>
						</div>

						<div id="contetRight">Insert the password for store a proxy
							of you certificate into MyProxy server, don't forgot this
							password.</div>
						<div id="reset"  style="margin-bottom: 15px;"></div>

						<input type="submit" value="Get Certificate"  onclick="loading();"/>
					</form:form>

				</div>


			</div>	
		</div>
			<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>

			<%@ include file="/WEB-INF/jsp/footer.jsp"%>
		<div id="wrapper">
			<div id="overlay" style="display:none;"></div>
			<div  id="popup" style="display:none; left:18%;">
				<img src="<c:url value='/images/loading.gif'/>" />
			</div>
			
		</div>
	</div>
	
	
</body>
</html>




