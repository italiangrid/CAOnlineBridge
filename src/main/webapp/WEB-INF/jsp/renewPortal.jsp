<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>IGI CA online - Certificate Renewal</title>
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

		<c:if test="${revokeRequest.portalRequest==false }">
			<%@ include file="/WEB-INF/jsp/header.jsp"%>
		</c:if>
		
		<div id="content">
			<h2>Renew Certificate</h2>
			
			<div id="reset"></div>
			<div id="personalInfo">
				<div class="profile">Certificate Information:</div>
				<table>
					<tr>
						<td><strong>Subject DN:</strong><br/> ${revokeRequest.subjectDN }</td>
					</tr>
					<tr>
						<td><strong>Issuer DN:</strong><br/> ${revokeRequest.issuerDN }</td>
					</tr>
					<c:if test="${revokeRequest.certificateSN!=null }">
					<tr>
						<td><strong>Certificate SN:</strong><br/>  ${revokeRequest.certificateSN }</td>
					</tr>
					</c:if>
		
				</table>
			</div>
			<div id="reset"  style="margin-bottom: 15px;"></div>
			
			<c:if test="${showRequest==false }">
				<div class="errorblock">You can renew your certificate only one month before the expiration date. </div>
			</c:if>
			
			<c:if test="${showRequest == true }">
				<c:if test="${revokeRequest.portalRequest==false }">	
				<c:set var="browser" value="${header['User-Agent']}" scope="session"/>
				<c:choose>
				<c:when test="${fn:contains(header['User-Agent'],'MSIE')}">
					<script type="text/javascript">

					   function CreateRequest() 
					   {
					                          
					
					     try {
					       // Variables
					       var objCSP = objCertEnrollClassFactory.CreateObject("X509Enrollment.CCspInformation");
					       var objCSPs = objCertEnrollClassFactory.CreateObject("X509Enrollment.CCspInformations");
					       var objPrivateKey = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX509PrivateKey");
					       var objRequest = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX509CertificateRequestPkcs10")
					       var objObjectIds = objCertEnrollClassFactory.CreateObject("X509Enrollment.CObjectIds");
					       var objObjectId = objCertEnrollClassFactory.CreateObject("X509Enrollment.CObjectId");
					       var objX509ExtensionEnhancedKeyUsage = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX509ExtensionEnhancedKeyUsage");
					       var objExtensionTemplate = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX509ExtensionTemplateName")
					       var objDn = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX500DistinguishedName")
					       var objEnroll = objCertEnrollClassFactory.CreateObject("X509Enrollment.CX509Enrollment")
					
					       //  Initialize the csp object using the desired Cryptograhic Service Provider (CSP)
					       objCSP.InitializeFromName("Microsoft Enhanced Cryptographic Provider v1.0");
					
					       //  Add this CSP object to the CSP collection object
					       objCSPs.Add(objCSP);
					
					       //  Provide key container name, key length and key spec to the private key object
					       //objPrivateKey.ContainerName = "AlejaCMa";
					       objPrivateKey.Length = 2048;
					       objPrivateKey.KeySpec = 1; // AT_KEYEXCHANGE = 1
					
					       //  Provide the CSP collection object (in this case containing only 1 CSP object)
					       //  to the private key object
					       objPrivateKey.CspInformations = objCSPs;
					
					       // Initialize P10 based on private key
					       objRequest.InitializeFromPrivateKey(1, objPrivateKey, ""); // context user = 1
					
					       // 1.3.6.1.5.5.7.3.2 Oid - Extension
					       objObjectId.InitializeFromValue("1.3.6.1.5.5.7.3.2");
					       objObjectIds.Add(objObjectId);
					       objX509ExtensionEnhancedKeyUsage.InitializeEncode(objObjectIds);
					       objRequest.X509Extensions.Add(objX509ExtensionEnhancedKeyUsage);
					
					       // 1.3.6.1.5.5.7.3.3 Oid - Extension
					       //objExtensionTemplate.InitializeEncode("1.3.6.1.5.5.7.3.3");
					       //objRequest.X509Extensions.Add(objExtensionTemplate);
					
					       // DN related stuff
					       objDn.Encode("CN=${certificateRequest.cn},OU=${certificateRequest.l},O=${certificateRequest.o},O=MICS,DC=IGI,DC=IT ", 0); // XCN_CERT_NAME_STR_NONE = 0
					       objRequest.Subject = objDn;
					
					       // Enroll
					       objEnroll.InitializeFromRequest(objRequest);
					       var pkcs10 = objEnroll.CreateRequest(3); // XCN_CRYPT_STRING_BASE64REQUESTHEADER = 3
					
					       //document.write("<br>" + pkcs10);
					       document.getElementById("spkac").setAttribute("value", pkcs10);
					       
					       return pkcs10;
					     }
					     catch (ex) {
					       document.write("<br>" + ex.description);
					       return false;
					     }
					
					     return true;
					   }       
					
					   //CreateRequest();
					
					 </script>
					 
					<c:url var="saveUrl" value="/RenewPortal/certRenew" />
					<form method="POST"
						action="${saveUrl }">
						
					
						<div id="reset"></div>
						<div id="personalInfo">
						<div class="profile">Personal Information:</div>
							<table>
								<tr>
									<td>Mail: ${certificateRequest.mail }</td>
									<td>
									<spring:bind path="certificateRequest.mail">
	   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
	        						</spring:bind>
									</td>
								</tr>
					
								<tr>
									<td>CN:  ${certificateRequest.cn }</td>
									<td><spring:bind path="certificateRequest.cn">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind></td>
								</tr>
								
								<tr>
									<td>O:  ${certificateRequest.o }</td>
									<td><spring:bind path="certificateRequest.o">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind></td>
								</tr>
					
								<tr>
									<td>L:  ${certificateRequest.l }</td>
									<td><spring:bind path="certificateRequest.l">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind></td>
								</tr>
								
								<tr>
									<td>C: IT</td>
									<td></td>
								</tr>
							</table>
						</div>
						<!-- <div id="contetRight">
							Your personal data retreived from your Identity Provider.
						</div> -->
						<div id="reset"  style="margin-bottom: 15px;"></div>
						<div style="display: none;">
						<strong>Key selection:</strong> 
						<div id="reset" ></div>
						<div id="keygen">
							<table>
					
								<tr>
									<td></td>
									<td><spring:bind path="certificateRequest.proxyPass1">
		   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
		        						</spring:bind></td>
								</tr>
					
								<tr>
									<td></td>
									<td>
									<spring:bind path="certificateRequest.proxyPass2">
	   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
	        						</spring:bind>
									</td>
								</tr>
					
							</table>
						</div>
					
						<div id="contetRight">
							Select the Key strength.
						</div>
						<div id="reset"></div>
						</div>
						
						<input type="hidden" id="spkac" name="spkac" value=""/>
						
						<spring:bind path="revokeRequest.subjectDN">
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.issuerDN" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.certificateSN" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.reason" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.portalRequest" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.accepted" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
      					<spring:bind path="certificateRequest.persistentId" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
      					<spring:bind path="certificateRequest.fromPortal" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>	
						<p>
						<spring:bind path="certificateRequest.conditionTerm" >
 									<input type="checkbox" name="${status.expression}" value="true"/>
      						</spring:bind>
						I have read and I accept the <a href="#">Condition Term of Use</a>.</p>
						<input type="submit" value="Get Certificate" onclick="loading();"/>
					</form>
					
					 <script type="text/javascript">
					 CreateRequest();
					 </script>
					 
					 
				</c:when>
				<c:otherwise>
					
					<c:url var="saveUrl" value="/RenewPortal/certRenew" />

					<form method="POST"
						action="${saveUrl }">
						
						<div id="reset"></div>
						<div id="personalInfo">
							<div class="profile">Personal Information:</div>
							<table>
								<tr>
									<td>Mail: ${certificateRequest.mail }</td>
									<td>
									<spring:bind path="certificateRequest.mail">
	   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
	        						</spring:bind>
									</td>
								</tr>
					
								<tr>
									<td>CN:  ${certificateRequest.cn }</td>
									<td><spring:bind path="certificateRequest.cn">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind></td>
								</tr>
								
								<tr>
									<td>O:  ${certificateRequest.o }</td>
									<td><spring:bind path="certificateRequest.o">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind></td>
								</tr>
					
								<tr>
									<td>L:  ${certificateRequest.l }</td>
									<td><spring:bind path="certificateRequest.l">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind></td>
								</tr>
								
								<tr>
									<td>C: IT</td>
									<td></td>
								</tr>
							</table>
						</div>
						<!-- <div id="contetRight">
							Your personal data retreived from your Identity Provider.
						</div> -->
						<div id="reset"  style="margin-bottom: 15px;"></div>
						<div style="display: none;">
						<strong>Key selection:</strong> 
						<div id="reset" ></div>
						<div id="keygen">
							<table>
								<tr>
									<td>Key strength:</td>
									<td><keygen id="spkac" name="spkac"
											challenge="TheChallenge1000" /></td>
								</tr>
					
								<tr>
									<td></td>
									<td><spring:bind path="certificateRequest.proxyPass1">
		   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
		        						</spring:bind></td>
								</tr>
					
								<tr>
									<td></td>
									<td>
									<spring:bind path="certificateRequest.proxyPass2">
	   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
	        						</spring:bind>
									</td>
								</tr>
					
							</table>
						</div>
					
						<div id="contetRight">
							Select the Key strength.
						</div>
						<div id="reset"></div>
						</div>
						
						<spring:bind path="revokeRequest.subjectDN">
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.issuerDN" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.certificateSN" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.reason" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.portalRequest" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
						<spring:bind path="revokeRequest.accepted" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
      					<spring:bind path="certificateRequest.persistentId" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>
      					<spring:bind path="certificateRequest.fromPortal" >
 									<input type="hidden" name="${status.expression}" value="${status.value}"/>
      						</spring:bind>	
						<p>
						<spring:bind path="certificateRequest.conditionTerm" >
 									<input type="checkbox" name="${status.expression}" value="true"/>
      						</spring:bind>
						I have read and I accept the <a href="#">Condition Term of Use</a>.</p>
						<input type="submit" value="Get Certificate" onclick="loading();"/>
					</form>

				</c:otherwise>
				</c:choose>
				</c:if>
				
				<c:if test="${revokeRequest.portalRequest==true }">
					<div id="contentLeft2">
	
						<c:url var="saveUrl" value="/RenewPortal/certRenew" />
						<form method="POST"
							action="${saveUrl }">
							
							<div id="password2">
								We are going to provide you of the necessary credentials.<br/>
								Please insert a password below and click on "<strong>Get Credentials</strong>" button. <br/><br/>
								<strong>Don't forget this password:</strong> it will be asked again to use Grid and Cloud resources
								 in a secure way and it will be not saved in the system.
								<br/><br/>
								<form:errors path="*" cssClass="errorblock" element="div" />
								<table>
									<tr>
										<td><strong>Insert Password</strong></td>
									</tr>
									<tr>
										<td>
										<spring:bind path="certificateRequest.proxyPass1">
		   									<input type="password" name="${status.expression}" value=""/>
		        						</spring:bind>
										</td>
									</tr>
	
									<tr>
										<td><strong>Retype Password</strong></td>
									</tr>
									<tr>
										<td>
										<spring:bind path="certificateRequest.proxyPass2">
		   									<input type="password" name="${status.expression}" value=""/>
		        						</spring:bind>
										</td>
									</tr>
	
								</table>
								
								<spring:bind path="certificateRequest.mail">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
        						<spring:bind path="certificateRequest.cn">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
        						<spring:bind path="certificateRequest.o">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
        						<spring:bind path="certificateRequest.l">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
								
								
								<spring:bind path="revokeRequest.subjectDN">
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
								<spring:bind path="revokeRequest.issuerDN" >
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
								<spring:bind path="revokeRequest.certificateSN" >
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
								<spring:bind path="revokeRequest.reason" >
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
								<spring:bind path="revokeRequest.portalRequest" >
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
								<spring:bind path="revokeRequest.accepted" >
   									<input type="hidden" name="${status.expression}" value="${status.value}"/>
        						</spring:bind>
        						<spring:bind path="certificateRequest.persistentId" >
										<input type="hidden" name="${status.expression}" value="${status.value}"/>
	     						</spring:bind>
	     						<spring:bind path="certificateRequest.fromPortal" >
										<input type="hidden" name="${status.expression}" value="${status.value}"/>
	     						</spring:bind>	
								<br/>
								<p>
								<spring:bind path="certificateRequest.conditionTerm" >
   									<input type="checkbox" name="${status.expression}" value="true"/>
        						</spring:bind>
								I have read and I accept the <a href="#">Condition Term of Use</a>.</p>
								<input class="buttonCA" type="submit" value="Get Credentials"  onclick="loading();"/>
							</div>
	
							<div id="contetRight2">
							
							<script  type="text/javascript">
						 
							 function openNewWindow() {
							 popupWin = window.open('https://portal.italiangrid.it:8443/info/certificate-upload-technical-info.html',
							 'open_window',
							 'scrollbars, resizable, dependent, width=640, height=480, left=0, top=0')
							 }
							 
							 </script>
							
							<a href="javascript:openNewWindow();"><img src="<c:url value='/images/Information2.png'/>" height="64"/>Technical Information</a></div>
							<div id="reset"  style="margin-bottom: 15px;"></div>
							
							
						</form>
	
					</div>
				</c:if>
			</c:if>
		</div>
		<c:if test="${revokeRequest.portalRequest==false }">
			<%@ include file="/WEB-INF/jsp/rightMenu.jsp"%>
	
			<%@ include file="/WEB-INF/jsp/footer.jsp"%>
		</c:if>
		

	</div>
	
	
</body>
</html>
