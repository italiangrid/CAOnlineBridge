<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>IGI CA online - Certificate Revocation</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/main.css' />">

</head>
<body>

	<div id="page">

		<c:if test="${revokeRequest.portalRequest==false }">
			<%@ include file="/WEB-INF/jsp/header.jsp"%>
		</c:if>
		<h2>Revoke Certificate</h2>
		<div id="content">

			<c:url var="revokeUrl" value="/Revoke/certRevoke" />
			<form:form modelAttribute="revokeRequest" method="POST"
				action="${revokeUrl }">
				<form:errors path="*" cssClass="errorblock" element="div" />
				<strong>Certificate Informations:</strong>
				<div id="reset"></div>
				<div id="personalInfo">
					<table>
						<tr>
							<td><form:label path="subjectDN"><strong>Subject DN:</strong><br/> ${revokeRequest.subjectDN }</form:label></td>
							<td><form:hidden path="subjectDN" /></td>
						</tr>
						<tr>
							<td><form:label path="issuerDN"><strong>Issuer DN:</strong><br/> ${revokeRequest.issuerDN }</form:label></td>
							<td><form:hidden path="issuerDN" /></td>
						</tr>
						<c:if test="${revokeRequest.certificateSN!=null }">
						<tr>
							<td><form:label path="certificateSN"><strong>Certificate SN:</strong><br/>  ${revokeRequest.certificateSN }</form:label></td>
							<td><form:hidden path="certificateSN" /></td>
						</tr>
						</c:if>
						<tr>
							<td colspan="2"><form:label path="reason">Specify revocation reason:</form:label>
								<form:select path="reason">
									<form:option value="0">Unspecified</form:option>
									<form:option value="10">AA Compromise</form:option>
									<form:option value="3">Affiliation Changed</form:option>
									<form:option value="2">CA Compromise</form:option>
									<form:option value="6">Certificate Hold</form:option>
									<form:option value="5">Cessation Of Operation</form:option>
									<form:option value="1">Key Compromise</form:option>
									<form:option value="9">Privileges With Drawn</form:option>
									<form:option value="8">Remove From CRL</form:option>
									<form:option value="4">Superseded</form:option>
								</form:select>
							</td>
						</tr>
			
					</table>
				</div>
				
				<div id="reset"  style="margin-bottom: 15px;"></div>
				
				<p>
				<form:checkbox path="accepted" />
				<form:label path="accepted">I'm sure to revoke my certificate.</form:label></p>
				
				<form:hidden path="portalRequest" />
				<input type="submit" value="Revoke and Delete Certificate"/>
			</form:form>
			
		</div>
		<c:if test="${revokeRequest.portalRequest==false }">
			<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>
	
			<%@ include file="/WEB-INF/jsp/footer.jsp"%>
		</c:if>
		

	</div>
	
	
</body>
</html>
