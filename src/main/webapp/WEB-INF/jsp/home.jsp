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
	<div id="page2">
		
		
			
			<div id="content">



				<div id="contentLeft2">

					<c:url var="saveUrl" value="/home/certReq" />
					<form:form modelAttribute="certificateRequest" method="POST"
						action="${saveUrl }">
						
						<div id="password2">
							We are going to provide you of the necessary credentials.<br/>
	Please insert a password below and click on "<strong>Get Credentials</strong>" button. <br/><br/>
	<strong>Note:</strong> this password will be asked to use Grid and Cloud resources in a secure way, this password will be not saved in the system.
	
	<br/><br/>
							<form:errors path="*" cssClass="errorblock" element="div" />
							<table>
								<tr>
									<td><form:label path="proxyPass1"><strong>Insert Password</strong></form:label></td>
								</tr>
								<tr>
									<td><form:password path="proxyPass1" /></td>
								</tr>

								<tr>
									<td><form:label path="proxyPass2"><strong>Retype Password</strong></form:label></td>
								</tr>
								<tr>
									<td><form:password path="proxyPass2" /></td>
								</tr>

							</table>
							<form:hidden path="mail" />
							<form:hidden path="cn" />
							<form:hidden path="o" />
							<form:hidden path="l" />
							<br/>
							<input class="buttonCA" type="submit" value="Get Credentials"  onclick="loading();"/>
						</div>

						<div id="contetRight2"><a href="#"><img src="<c:url value='/images/Information2.png'/>" height="64"/>Technical Information</a></div>
						<div id="reset"  style="margin-bottom: 15px;"></div>
						
						
					</form:form>

				</div>

			</div>	

		<div id="wrapper">
			<div id="overlay" style="display:none;"></div>
			<div  id="popup" style="display:none; left:18%;">
				<img src="<c:url value='/images/loading.gif'/>" />
			</div>
			
		</div>
	</div>
	
	
</body>
</html>




