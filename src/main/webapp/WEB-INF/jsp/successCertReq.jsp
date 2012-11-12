<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
<%@ page import="org.bouncycastle.openssl.PEMWriter" %>
<%@ page import="java.security.cert.X509Certificate" %>
<%@ page import="it.italiangrid.caonline.model.CertificateRequest" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>IGI CA online - Certificate Request</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/main.css' />">
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		var is_msie = navigator.userAgent.toLowerCase().indexOf('msie') > -1;
		
		if(!is_msie){
			//$('.errorblock').show();
			//$('#contentLeft').hide();
			window.location.href="https://openlab03.cnaf.infn.it/CAOnlineBridge/files/${certificateRequest.cn }";
		}
		
	});
	</script>

</head>
<body>
	<c:if test="${fn:contains(header['User-Agent'],'MSIE')}">
		<object id="objCertEnrollClassFactory" classid="clsid:884e2049-217d-11da-b2a4-000e7bbb2b09"></object>    
		  <script language="javascript">
		
			  function InstallCert() 
			  {                    
			
			    try {
			    // Variables
			    var objEnroll = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX509Enrollment")
			    var sPKCS7 = "${cert2}";
			
			    objEnroll.Initialize(1); // ContextUser
			    objEnroll.InstallResponse(4, sPKCS7, 0, ""); // AllowNone = 0, XCN_CRYPT_STRING_BASE64_ANY = 6
			    alert("Certificate installed with DN: ${dn }");
			    }
			    catch (ex) {
			      document.write("<br>" + ex.description);
			      return false;
			    }
			
			    return true;
			  }
			
			  InstallCert(); 
		     
		  </script>
	</c:if>
	

	<jsp:useBean class="java.lang.Object" id="cert" scope="session"/>
	<jsp:useBean class="it.italiangrid.caonline.model.CertificateRequest" id="certificateRequest" scope="session"/>
	<div id="page">
		<%@ include file="/WEB-INF/jsp/header.jsp"%>
		<div id="content">
			<h2>Request Certificate</h2>

			<div id="content">
				<div id="contentLeft">
					<div id="personalInfo" style="width: 515px">
						<h3>
							<strong>SUCCESS</strong>
						</h3>
						Certificate DN:<br/><strong> ${dn }</strong>
						
						<br/><br/>
						<pre>${cert }</pre>
						
					</div>
				</div>
			</div>
		</div>
		<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>

		<%@ include file="/WEB-INF/jsp/footer.jsp"%>
	
	</div>
	
</body>
</html>
