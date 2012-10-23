package it.italiangrid.caonline.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.xml.namespace.QName;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ejbca.core.protocol.ws.client.gen.*;
import org.ejbca.core.protocol.ws.common.CertificateHelper;

public class EjbcaWSTest {

	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws NotFoundException_Exception
	 * @throws EjbcaException_Exception
	 * @throws CADoesntExistsException_Exception
	 * @throws AuthorizationDeniedException_Exception
	 * @throws CertificateException
	 * @throws WaitingForApprovalException_Exception
	 * @throws UserDoesntFullfillEndEntityProfile_Exception
	 * @throws ApprovalException_Exception
	 * @throws HardTokenDoesntExistsException_Exception
	 * @throws ApprovalRequestExpiredException_Exception
	 * @throws ApprovalRequestExecutionException_Exception
	 */
	public static void main(String[] args) throws MalformedURLException,
			AuthorizationDeniedException_Exception,
			CADoesntExistsException_Exception, EjbcaException_Exception,
			NotFoundException_Exception, CertificateException,
			ApprovalException_Exception,
			UserDoesntFullfillEndEntityProfile_Exception,
			WaitingForApprovalException_Exception,
			ApprovalRequestExecutionException_Exception,
			ApprovalRequestExpiredException_Exception,
			HardTokenDoesntExistsException_Exception {
		// TODO Auto-generated method stub

		String urlstr = "https://localhost:8443/ejbca/ejbcaws/ejbcaws?wsdl";

		Security.addProvider(new BouncyCastleProvider());
		// Security.setProperty("ssl.TrustManagerFactory.algorithm",
		// "AcceptAll");
		// Security.setProperty("ssl.KeyManagerFactory.algorithm",
		// "NewSunX509");

		System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
		System.setProperty("javax.net.ssl.keyStore",
				"/Users/dmichelotto/Desktop/p12/superadmin.p12");
		System.setProperty("javax.net.ssl.keyStorePassword", "ejbca");

		System.setProperty("javax.net.ssl.trustStore",
				"/Users/dmichelotto/Desktop/p12/truststore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

		QName qname = new QName("http://ws.protocol.core.ejbca.org/",
				"EjbcaWSService");
		EjbcaWSService service = new EjbcaWSService(new URL(urlstr), qname);
		EjbcaWS ejbcaws = service.getEjbcaWSPort();

		System.out.println("Versione Ejbaca: " + ejbcaws.getEjbcaVersion());

		UserDataVOWS user1 = new UserDataVOWS();
		user1.setUsername("userTestSpkac6");
		user1.setSubjectDN("CN=userTestSpkac6, OU=CNAF, O=INFN, O=MICS, DC=IGI, DC=IT");
		user1.setCaName("subca-benci");
		user1.setEmail(null);
		user1.setSubjectAltName(null);
		user1.setEndEntityProfileName("benci");
		user1.setCertificateProfileName("ENDUSER");
		user1.setPassword("userTestPasswd");
		user1.setClearPwd(false);
		user1.setStatus(UserDataVOWS.STATUS_NEW);
		user1.setTokenType(UserDataVOWS.TOKEN_TYPE_USERGENERATED);

		ejbcaws.editUser(user1);

		String spkac = " MIICTDCCATYwggEgMAsGCSqGSIb3DQEBAQOCAQ8AMIIBCgKCAQEAqrmZlYzNZOJ27n70wwydBqg9i9MPV2yS5jhC7Esvn7bPlOFzahRvPjykw6suSWJWyLAqPR0HbBCO5EgQnvKuQ9i5UQff4zFsprGJjQU0/3b/rAWGB4+Wi6N+qRojJ05upKxw617NhbMq8WcmwRxwYNHBIIuAJJbbLs1Py0M9F2JJXZrwUbtrzjEjSJb7mn+IOgVllU3wuL3uAHtXLourPUwAvTLdMQah1/6ipQOQ4rAQZH0E/X98Z2M7tGgFHRxDkMxa87owFJxFYR3WwKkryWbn+P9zU/5bpNHLDc5CykfxGcWfYxiTjx3DooCI5+7BNqvQiMwBgmtIg/6HpUy+ewIDAQABFhBUaGVDaGFsbGVuZ2UxMDAwMAsGCSqGSIb3DQEBBAOCAQEAHSPdkA2q+Pp1gQ59hth7zUZYhVGS+OLoIZym9BS6mpPPoYE1ArmD/FW6cnmhzK28ieSxkerm1QQw8V9HNip3SsDTpwdGReABCI1fJTczdenUpd0SY+uN+hV1vex1x3PGsEwL8asAahFozkmSAVQ9J8D3k9ykdhUkXAg+e+epaXBSLPN5c8qgI/MtZJFZn8oRLDDS5FXPMqtPWWWxOHFDESbSFHV+yqtV52je+PVK2OW61TJqeWWQinbWOPCVJf1lGqo0a6ftB6OQAnWWCqmMAiBoT8mSZEkvwHmThausGuRyebzlgk1gZtkhZYE9/f7M9xze5NuX30YWQYJtenEc6w==";

		CertificateResponse certenv = ejbcaws.spkacRequest("userTestSpkac6",
				"userTestPasswd", spkac, null,
				CertificateHelper.RESPONSETYPE_CERTIFICATE);

		X509Certificate cert = certenv.getCertificate();

		System.out.println("Certificate dn: " + cert.getSubjectDN().getName());

	}
}
