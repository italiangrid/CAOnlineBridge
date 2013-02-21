<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<object id="objCertEnrollClassFactory" classid="clsid:884e2049-217d-11da-b2a4-000e7bbb2b09"></object>    
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
 
 <c:url var="saveUrl" value="/certReq/certReq" />
<form:form modelAttribute="certificateRequest" method="POST"
	action="${saveUrl }">
 <form:errors path="*" cssClass="errorblock" element="div" />
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
				<td><form:hidden path="proxyPass1" /></td>
			</tr>

			<tr>
				<td></td>
				<td><form:hidden path="proxyPass2" /></td>
			</tr>

		</table>
	</div>

	<div id="contetRight">
		Select the Key strength.
	</div>
	<div id="reset"></div>
	</div>
	
	<input type="hidden" id="spkac" name="spkac" value=""/>
	
	<p>
	<form:checkbox path="conditionTerm" />
	<form:label path="conditionTerm">I have read and I accept the <a href="#">Condition Term of Use</a>.</form:label></p>
	<input type="submit" value="Get Certificate" onclick="loading();"/>
</form:form>

 <script type="text/javascript">
 CreateRequest();
 </script>