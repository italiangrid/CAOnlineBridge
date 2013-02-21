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

</head>
<body>
	<div id="page">
		<c:if test="${revokeRequest.portalRequest==false }">
			<%@ include file="/WEB-INF/jsp/header.jsp"%>
		</c:if>
		<div id="content">
			

			<div id="content">
				<div id="contentLeft2">
					<div id="personalInfo2">
						<h3>
							<strong>SUCCESS</strong>
						</h3>
						<p>
						Your certificate successfully revoked.</p>
						<c:if test="${revokeRequest.portalRequest==true }">
							<p> Close this pop up window.</p>
						</c:if>
					</div>
				</div>
			</div>
		</div>
		<c:if test="${revokeRequest.portalRequest==false }">
			<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>
	
			<%@ include file="/WEB-INF/jsp/footer.jsp"%>
		</c:if>
		
	</div>
</body>
</html>
