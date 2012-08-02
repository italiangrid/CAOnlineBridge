<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>

<style>
.error {
	color: #ff0000;
}
 
.errorblock {
	color: #000;
	background-color: #ffEEEE;
	border: 3px solid #ff0000;
	padding: 8px;
	margin: 16px;
}
</style>

</head>
<body>
<h2>Hello World! I'm the CA-Online bridge, TOKEN</h2>

<br/>


<h2>Request Certificate</h2>

<c:url var="saveUrl" value="/home/certReq" />
<form:form modelAttribute="certificateRequest" method="POST" action="${saveUrl }">
	<form:errors path="*" cssClass="errorblock" element="div" />
	<table>
		<tr>
			<td><form:label path="mail">Mail: ${certificateRequest.mail }</form:label></td>
			<td><form:hidden path="mail"/></td>
		</tr>

		<tr>
			<td><form:label path="cn">CN:  ${certificateRequest.cn }</form:label></td>
			<td><form:hidden path="cn"/></td>
		</tr>
		
		<tr>
			<td><form:label path="o">O:  ${certificateRequest.o }</form:label></td>
			<td><form:hidden path="o"/></td>
		</tr>
		
		<tr>
			<td><form:label path="l">L:  ${certificateRequest.l }</form:label></td>
			<td><form:hidden path="l"/></td>
		</tr>
		
		<tr>
			<td><form:label path="proxyPass1">Proxy Password:</form:label></td>
			<td><form:input path="proxyPass1"/></td>
		</tr>
		
		<tr>
			<td><form:label path="proxyPass2">Retype Password:</form:label></td>
			<td><form:input path="proxyPass2"/></td>
		</tr>
		
	</table>
	
	<input type="submit" value="Get Certificate" />
</form:form>


</body>
</html>
