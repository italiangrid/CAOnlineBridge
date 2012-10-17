package it.italiangrid.caonline.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Security;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.netscape.NetscapeCertRequest;
import org.bouncycastle.util.encoders.Base64;
import org.glite.security.util.DNHandler;
import org.globus.gsi.GlobusCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import it.italiangrid.caonline.model.CertificateRequest;
import it.italiangrid.caonline.util.RequestCertificateUtil;
import it.italiangrid.caonline.util.TokenCreator;
import it.italiangrid.caonline.util.VOMSAdminCallOut;
import it.italiangrid.portal.dbapi.domain.Certificate;
import it.italiangrid.portal.dbapi.domain.UserInfo;
import it.italiangrid.portal.dbapi.domain.UserToVo;
import it.italiangrid.portal.dbapi.domain.Vo;
import it.italiangrid.portal.dbapi.services.CertificateService;
import it.italiangrid.portal.dbapi.services.UserInfoService;
import it.italiangrid.portal.dbapi.services.UserToVoService;
import it.italiangrid.portal.dbapi.services.VoService;

/**
 * This class is the Controller of the CAOnlineBridge.
 * Use the Spring MVC 3 pattern for developing Servelet.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 *
 */

@Controller("homeController")
public class HomeController {
	
	/**
	 * Logger attribute
	 */
	private static final Logger log = Logger
			.getLogger(HomeController.class);
	
	private static String NOPASSWORD = "noPasswd";
	
	@Autowired
	private VoService voService;
	
	@Autowired
	private UserToVoService userToVoService;
	
	@Autowired
	private CertificateService certificateService;
	
	@Autowired
	private UserInfoService userInfoService;
	
	/**
	 * 
	 * This method return two different page depends if the use directly access to the servlet or passing from the portal.
	 * 
	 * @param request - the request of the session.
	 * @param model - passing a {@link CertificateRequest} initialized with the Shibboleth headers value.
	 * @return the home.jsp if receive two valid token from the portal or the certReq.jsp if don't receive the tokens
	 */
	@RequestMapping(value="/home")
	public String showHome(HttpServletRequest request, Model model) {
		log.info("Home controller");
		if((request.getParameter("t1")!=null)&&(request.getParameter("t2")!=null)){
			
			log.info("Token Validation");
			
			String token1 = request.getParameter("t1");
			String token2 = request.getParameter("t2");
			
			log.info("Received t1: " + token1 + " & t2: " + token2);
			
			String userSecret = request.getHeader("mail");
			
			String verifyToken = TokenCreator.getToken(userSecret);
			log.info("Created token: " + verifyToken);
			
			if((token1.equals(verifyToken))||(token2.equals(verifyToken))){
				model.addAttribute("certificateRequest", new CertificateRequest(request.getHeader("mail"),request.getHeader("cn"),request.getHeader("l"),request.getHeader("o"), null, null));
				return "home";
			}else
				return "error";
		}
		model.addAttribute("certificateRequest", new CertificateRequest(request.getHeader("mail"),request.getHeader("cn"),request.getHeader("l"),request.getHeader("o"), NOPASSWORD, NOPASSWORD));
		return "certReq";
	}
	
	/**
	 * This method receive the user parameter and the password for the proxy encryption for the MyProxy server where the proxy will be stored and start the request procedures.
	 * 
	 * @param certificateRequest - getting from the form the fields value
	 * @param result - validating the {@link CertificateRequest} in result there are the errors. 
	 * @param model - getting the model from the request.
	 * @return the success page if all is ok or return to the form page if some problem are occurred.
	 */
	@RequestMapping(value = "/home/certReq", method = RequestMethod.POST)
    public String createCertificateAndProxy(@Valid @ModelAttribute("certificateRequest") CertificateRequest certificateRequest, BindingResult result, Model model) {
		log.info("Received request of new Certificate and new Proxy");
		
		if (result.hasErrors()) {
			model.addAttribute("certificateRequest", certificateRequest);
			log.info("Not valid request");
			return "home";
			
		} else {
			
			log.info("Received: \n" + certificateRequest.toString());
//			RequestCertificateUtil reqCert = new RequestCertificateUtil();
//			if(RequestCertificateUtil.doRequestAndStore(certificateRequest,result)){
//				return "success";
//			}
			
//			GlobusCredential credential = RequestCertificateUtil.doRequestAndStore(certificateRequest,result);
			
			String certPath = RequestCertificateUtil.getCertificate(certificateRequest, result);
			
			if(certPath == null){
				result.reject("doRequestAndStore.getCertificato", "Certificate not released");
				return "home";
			}
			
			GlobusCredential credential = RequestCertificateUtil.getCredential(certPath, result);
			
			if(credential == null){
				result.reject("doRequestAndStore.putCertificate", "Certificate not stored into MyProxy");
				return "home";
			}
			
			
			String usernameCert = insertCertificate(credential, certificateRequest.getMail());
			
			if(usernameCert == null){
				result.reject("doRequestAndStore.putCertificateIntoDB", "Certificate not stored into Portal DB");
				return "home";
			}
			
			if(!RequestCertificateUtil.putCertificate(credential, result, certificateRequest.getProxyPass1(), usernameCert)){
				result.reject("doRequestAndStore.putCertificate", "Certificate not stored into MyProxy");
			}
			
			//VOMSAdminCallOut.test();
			
			if(!VOMSAdminCallOut.putUser(credential.getIdentityCertificate(), certificateRequest.getMail(), certificateRequest.getCn())){
				result.reject("doRequestAndStore.putUserVOMS", "User not stored in VOMS");
			}
			
			insertVOMS(certificateRequest.getMail(), DNHandler.getSubject(credential.getIdentityCertificate()).getX500());
			
			
			if(RequestCertificateUtil.deleteDirectory(new File(certPath))){
				result.reject("doRequestAndStore.deleteDirectory", "Temporary directory don't deleted");
			}
			
			activateUser(certificateRequest.getMail());
			
			return "success";
		}
		
	}
	
	
	private void insertVOMS(String mail, String subject) {
		
		UserInfo userInfo = userInfoService.findByMail(mail);
		
		Vo vo = voService.findByName("vomstest");
	
		log.debug("Vo = " + vo.getVo() + " : " + vo.getIdVo());
		log.debug("Subject: " + subject);
		
		
		
		
		userToVoService.save(userInfo.getUserId(), vo.getIdVo(), subject);
		
		List<UserToVo> utvo = userToVoService.findById(userInfo.getUserId());
		
		if(utvo.size()==1)
			userToVoService.setDefault(userInfo.getUserId(), vo.getIdVo());
		
	}

	/**
	 * This method receive the user parameter and start the request procedures.
	 * 
	 * @param certificateRequest - getting from the form the fields value
	 * @param result - validating the {@link CertificateRequest} in result there are the errors. 
	 * @param model - getting the model from the request.
	 * @return the success page if all is OK or return to the form page if some problem are occurred.
	 * @throws IOException 
	 */
	@RequestMapping(value = "/certReq/certReq", method = RequestMethod.POST)
    public String createCertificate(@Valid @ModelAttribute("certificateRequest") CertificateRequest certificateRequest, BindingResult result, Model model, HttpServletRequest request) throws IOException {
		log.info("Received request of new Certificate");
		
		if (result.hasErrors()) {
			model.addAttribute("certificateRequest", certificateRequest);
			log.info("Not valid request");
			return "certReq";
			
		} else {
			
			log.info("Received from certReq: \n" + certificateRequest.toString());
			log.info("keygen: " + request.getParameter("spkac"));
			log.info("keygen: " + Base64.decode(request.getParameter("spkac")).hashCode());
			
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			
			NetscapeCertRequest req = new NetscapeCertRequest(Base64.decode(request.getParameter("spkac")));
			
			PublicKey pubKey = req.getPublicKey();
			
			log.info("Pubkey: " + pubKey.toString());
			
			/*byte[] bytes = pubKey.getEncoded();
			
			FileOutputStream keyfos = new FileOutputStream("/root/testReq.csr");
			keyfos.write(bytes);
			keyfos.close();
			
			String print = "";
			
			for (byte b : bytes) {
				print += b;
			}
			
			log.info("request: "  + print);*/
			
			/*
			 * new NetscapeCertRequest(Base64.decode(spkacData)).getPublicKey()
			 */
			
			
			return "successCertReq";
//			return "certReq";
		}
		
	}
	

private String insertCertificate(GlobusCredential credential, String mail){
		
		UserInfo userInfo = userInfoService.findByMail(mail);
//		UserInfo userInfo = userInfoService.findByUsername("4324127127c021ad8b53ff2b2d38aa5d5944cd65");
        
        int uid = userInfo.getUserId();
       
        java.security.cert.X509Certificate[] certs = credential.getCertificateChain();
        
        java.security.cert.X509Certificate cert = certs[0];
        
        List<Certificate> certificates = certificateService.findById(uid);
        
        log.info("/"+credential.getSubject().replaceAll(", ", "/"));
        
        Certificate newCertificate = new Certificate(userInfo, DNHandler.getSubject(cert).getX500(), cert.getNotAfter(), "true", certificates.isEmpty()?"true":"false", DNHandler.getIssuer(credential.getIdentityCertificate()).getX500(), null);
        
        int id = certificateService.save(newCertificate, uid);
        
        if(id!=-1)
        	return certificateService.findByIdCert(id).getUsernameCert();
        	
		
		return null;
	}



	private boolean activateUser(String mail){
		
		UserInfo userInfo = userInfoService.findByMail(mail);

		userInfo.setRegistrationComplete("true");

		userInfoService.save(userInfo);
	    	
		return false;
	}

}
