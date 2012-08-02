package it.italiangrid.caonline.util;

import it.italiangrid.portal.dbapi.domain.Certificate;
import it.italiangrid.portal.dbapi.domain.UserInfo;
import it.italiangrid.portal.dbapi.services.CertificateService;
import it.italiangrid.portal.dbapi.services.UserInfoService;

import org.globus.gsi.GlobusCredential;
import org.springframework.beans.factory.annotation.Autowired;

public class InsertCertificateIntoDB {
	
	@Autowired
	private CertificateService certificateService;
	
	@Autowired
	private UserInfoService userInfoService;
	
	boolean insertCertificate2(GlobusCredential credential, String mail){
		
		UserInfo userInfo = userInfoService.findByMail(mail);
        
        int uid = userInfo.getUserId();
       
        java.security.cert.X509Certificate[] certs = credential.getCertificateChain();
        
        java.security.cert.X509Certificate cert = certs[0];
        
        Certificate newCertificate = new Certificate(userInfo, credential.getSubject(), cert.getNotAfter(), "true", "true", credential.getIssuer(), null);
        
        int id = certificateService.save(newCertificate, uid);
        
        if(id!=-1)
        	return true;
		
		return false;
	}

}
