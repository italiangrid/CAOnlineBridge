package it.italiangrid.caonline.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.openssl.PEMWriter;
import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.glite.security.util.DNHandler;
import org.globus.gsi.GlobusCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import it.italiangrid.caonline.model.CertificateRequest;
import it.italiangrid.caonline.util.EjbCAException;
import it.italiangrid.caonline.util.PKCS10CertificateRequest;
import it.italiangrid.caonline.util.RequestCertificateUtil;
import it.italiangrid.caonline.util.SpkacCertificateRequest;
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
	 * @throws WaitingForApprovalException_Exception 
	 * @throws UserDoesntFullfillEndEntityProfile_Exception 
	 * @throws ApprovalException_Exception 
	 * @throws NotFoundException_Exception 
	 * @throws EjbcaException_Exception 
	 * @throws CADoesntExistsException_Exception 
	 * @throws AuthorizationDeniedException_Exception 
	 * @throws NoSuchProviderException 
	 * @throws CertificateException 
	 * @throws IOException 
	 * @throws IllegalQueryException_Exception 
	 */
	@RequestMapping(value = "/home/certReq", method = RequestMethod.POST)
    public String createCertificateAndProxy(@Valid @ModelAttribute("certificateRequest") CertificateRequest certificateRequest, BindingResult result, Model model) throws CertificateException, NoSuchProviderException, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, ApprovalException_Exception, UserDoesntFullfillEndEntityProfile_Exception, WaitingForApprovalException_Exception, IOException, IllegalQueryException_Exception {
		log.info("Received request of new Certificate and new Proxy");
		
		if (result.hasErrors()) {
			model.addAttribute("certificateRequest", certificateRequest);
			log.info("Not valid request");
			return "home";
			
		} else {
			
//			log.info("Received: \n" + certificateRequest.toString());
			
			GlobusCredential credential = null;
			try {
				credential = RequestCertificateUtil.getCredential(certificateRequest, result);
			} catch (EjbCAException e) {
				result.reject("Exception", e.getMessage());
				model.addAttribute("certificateRequest", certificateRequest);
				return "home";
			}
			
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
			
			if(!VOMSAdminCallOut.putUser(credential.getIdentityCertificate(), certificateRequest.getMail(), certificateRequest.getCn())){
				result.reject("doRequestAndStore.putUserVOMS", "User not stored in VOMS");
			}
			
			insertVOMS(certificateRequest.getMail(), DNHandler.getSubject(credential.getIdentityCertificate()).getX500());
			
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
	 * @throws NoSuchProviderException 
	 */
	@RequestMapping(value = "/certReq/certReq", method = RequestMethod.POST)
    public String createCertificate(@Valid @ModelAttribute("certificateRequest") CertificateRequest certificateRequest, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException, NoSuchProviderException {
		log.info("Received request of new Certificate");
		
		if (result.hasErrors()) {
			model.addAttribute("certificateRequest", certificateRequest);
			log.info("Not valid request");
			return "certReq";
			
		} else {
			
			log.info("Received from certReq: \n" + certificateRequest.toString());
			
			try {
				X509Certificate cert = null;
				if(request.getHeader("User-Agent").contains("MSIE")){
					log.info("spkac=" + request.getParameter("spkac"));
					PKCS10CertificateRequest csr = new PKCS10CertificateRequest(certificateRequest);
					cert = csr.getX509Certificate(request.getParameter("spkac"));
				} else {
					SpkacCertificateRequest csr = new SpkacCertificateRequest(certificateRequest);
					cert = csr.getX509Certificate(request.getParameter("spkac"));
				}
				if(cert==null){
					result.reject("CertificateError", "Certificate already requested");
					return "certReq";
				}
				
				log.info(cert.getSubjectDN());
				
				model.addAttribute("dn", cert.getSubjectDN());
				
				StringWriter sw = new StringWriter();
                PEMWriter pemWriter = new PEMWriter(sw);
                pemWriter.writeObject(cert);
                pemWriter.close();
                String pemCert = sw.toString();
				
				log.info(pemCert.trim());
				
				model.addAttribute("cert",pemCert);
				model.addAttribute("cert2",pemCert.replaceAll("\n", ""));
				
				certificateRequest.setCert(cert);
				
				model.addAttribute(certificateRequest);
				if(!request.getHeader("User-Agent").contains("MSIE")){
					FileOutputStream certificate = new FileOutputStream("/etc/pki/tls/certs/CAOnlineBridge/"+ certificateRequest.getCn().hashCode()+".pem");
					
					if(request.getHeader("User-Agent").contains("Firefox")){
						PEMWriter pemWriter2 = new PEMWriter(new OutputStreamWriter(certificate));
						pemWriter2.writeObject(cert);
		                pemWriter2.close();
					} else {
		                DEROutputStream derCertificate = new DEROutputStream(certificate);
		                derCertificate.write(cert.getEncoded());
		                derCertificate.close();
					}
				}
				
				return "successCertReq";
			} catch (CertificateException e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (AuthorizationDeniedException_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (CADoesntExistsException_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (EjbcaException_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (NotFoundException_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (ApprovalException_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (UserDoesntFullfillEndEntityProfile_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (WaitingForApprovalException_Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (IllegalQueryException_Exception e) {
				result.reject("Exception", "User matching problem");
				e.printStackTrace();
			} catch (EjbCAException e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				result.reject("Exception", "Problem with CA connection");
				e.printStackTrace();
			} 
			
			return "certReq";
		}
		
	}
	
	
	@RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
	public void getFile(
	    @PathVariable("file_name") String fileName, 
	    HttpServletResponse response) {
		
		response.addHeader("Content-Type", "application/x-x509-user-cert");
		
		File file = new File("/etc/pki/tls/certs/CAOnlineBridge/"+ fileName.hashCode()+".pem");
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(file);
		
			OutputStream out = response.getOutputStream();
			 
			byte[] outputByte = new byte[4096];
			//copy binary content to output stream
			while(fileIn.read(outputByte, 0, 4096) != -1)
			{
				out.write(outputByte, 0, 4096);
			}
			fileIn.close();
			out.flush();
			out.close();
			
			file.delete();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

private String insertCertificate(GlobusCredential credential, String mail){
		
		UserInfo userInfo = userInfoService.findByMail(mail);
        
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
