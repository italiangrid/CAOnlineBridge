<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>IGI CA online - Certificate Information</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/main.css' />">
	
	<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js"></script>
	
	<script type="text/javascript">
		
		
	var list = new Array();
	function change(divName){
		var i = 0;
		var newlist = new Array();
		var isPresent = false;
		for (i = 0; i < list.length; i++) {
			if (list[i] != divName) {
				newlist.push(list[i]);
			} else {
				isPresent = true;
			}
		}

		if (isPresent == false){
			list.push(divName);
			$("#cert_"+divName).show("slow");
			$("#link_"+divName).html("Hide Certificate");
		}else{
			list = newlist;
			$("#cert_"+divName).hide("slow");
			$("#link_"+divName).html("Show Certificate");
			
		}
	}
		
	</script>

</head>
<body>

	<div id="page">

		<%@ include file="/WEB-INF/jsp/header.jsp"%>
		
		
		<div id="content">

			<h2>Your Certificate<c:if test="${fn:length(certList)>1 }">s</c:if></h2>
			
			<c:forEach var="cert" items="${certList }" varStatus="i">
				<div id="personalInfo" class="someClass">
				
					<div class="infoCert">
						<div class="profile">${cert.profile }</div>
						<p>
						<strong>Subject:</strong><br/>${cert.subject }<br/>
						<strong>Issuer:</strong><br/>${cert.issuer }<br/>
						</p>
						<p>
						<strong>Creation Date:</strong> ${cert.creationDate }<br/>
						<strong>Expiration Date:</strong> ${cert.expirationDate }<br/>
						</p>
						<p class="link">
							<a href="#" id="link_${i.count}"onclick="change('${i.count}'); return false;">View Certificate</a>
						</p>
					</div>
					<div id="cert_${i.count}" onclick="change('${i.count}');" class="cert">
						${cert.certificate }
					</div>
				</div>
				<div id="reset"  style="margin-bottom: 15px;"></div>
			</c:forEach>
			
		</div>
		
		<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>

		<%@ include file="/WEB-INF/jsp/footer.jsp"%>

	</div>
	
	
</body>
</html>
