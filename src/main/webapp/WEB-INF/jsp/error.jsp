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
		<%@ include file="/WEB-INF/jsp/header.jsp"%>
		<div id="content">
			<h2>Request Certificate</h2>

			<div id="content">
				<div id="personalInfo">
				<div id="contentLeft"><h3><strong>ACCESS DENIED</strong></h3>
				If you come from the grid portal, <br/>
				please close this pop up window and retry. 
				
				</div>
				</div>
			</div>
		</div>
		<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>

		<%@ include file="/WEB-INF/jsp/footer.jsp"%>
	</div>

</body>
</html>
