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
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.openssl.PEMWriter;
import org.ejbca.core.model.ca.crl.RevokedCertInfo;
import org.glite.security.util.DNHandler;
import org.globus.gsi.GlobusCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import it.italiangrid.caonline.ejbca.EjbCAInformationRequest;
import it.italiangrid.caonline.ejbca.EjbCARevokeRequest;
import it.italiangrid.caonline.ejbca.PKCS10CertificateRequest;
import it.italiangrid.caonline.ejbca.SpkacCertificateRequest;
import it.italiangrid.caonline.model.CertificateRequest;
import it.italiangrid.caonline.model.RevokeRequest;
import it.italiangrid.caonline.util.DBInteracion;
import it.italiangrid.caonline.util.DateUtil;
import it.italiangrid.caonline.util.RequestCertificateUtil;
import it.italiangrid.caonline.util.TokenCreator;
import it.italiangrid.caonline.util.VOMSAdminCallOut;
import it.italiangrid.portal.dbapi.domain.Certificate;
import it.italiangrid.portal.dbapi.services.CertificateService;
import it.italiangrid.portal.dbapi.services.UserInfoService;
import it.italiangrid.portal.dbapi.services.UserToVoService;
import it.italiangrid.portal.dbapi.services.VoService;

/**
 * This class is the Controller of the CAOnlineBridge. Use the Spring MVC 3
 * pattern for developing Servelet.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */

@Controller("homeController")
public class HomeController {

	/** 
	 * Logger attribute.
	 */
	private static final Logger log = Logger.getLogger(HomeController.class);

	/**
	 * Password.
	 */
	private static final String NOPASSWORD = "noPasswd";
	
	/**
	 * Array size.
	 */
	private static final int BYTE_ARRAY_SIZE = 4096;
	
	/**
	 * Days in a month.
	 */
	private static final int MONTH = 30;
	
	/**
	 * Database service.
	 */
	@Autowired
	private VoService voService;

	/**
	 * Database service.
	 */
	@Autowired
	private UserToVoService userToVoService;

	/**
	 * Database service.
	 */
	@Autowired
	private CertificateService certificateService;

	/**
	 * Database service.
	 */
	@Autowired
	private UserInfoService userInfoService;

	/**
	 * This method return two different page depends if the use directly access
	 * to the servlet or passing from the portal.
	 * 
	 * @param request
	 *            - the request of the session.
	 * @param model
	 *            - passing a {@link CertificateRequest} initialized with the
	 *            Shibboleth headers value.
	 * @return the home.jsp if receive two valid token from the portal or the
	 *         certReq.jsp if don't receive the tokens
	 */
	@RequestMapping(value = "/home")
	public final String showHome(final HttpServletRequest request,
			final Model model) {
		log.debug("Home controller");
		
		
		log.error("persistent-id : "+ request.getHeader("persistent-id").split("!")[2]);
		if ((request.getParameter("t1") != null)
				&& (request.getParameter("t2") != null)) {

			log.debug("Token Validation");

			String token1 = request.getParameter("t1");
			String token2 = request.getParameter("t2");

			log.debug("Received t1: " + token1 + " & t2: " + token2);

			String userSecret = request.getHeader("mail");

			String verifyToken = TokenCreator.getToken(userSecret);
			log.debug("Created token: " + verifyToken);

			if ((token1.equals(verifyToken)) || (token2.equals(verifyToken))) {
				model.addAttribute(
						"certificateRequest",
						new CertificateRequest(request.getHeader("mail"),
								request.getHeader("l"), request.getHeader("o"), 
								request.getHeader("cn"), null, null, false, 
								request.getHeader( "persistent-id").split("!")[2], 
								true));
				
						/*
						 * CertificateRequest(String mail, String l, String o, String cn,
			String proxyPass1, String proxyPass2, boolean conditionTerm,
			String persistentId, boolean fromPortal)
						 * */
				return "home";
			} else {
				log.error("Intrusion detected from: " + request.getRemoteAddr());
				return "error";
			}
		}
		model.addAttribute(
				"certificateRequest",
				new CertificateRequest(request.getHeader("mail"),
						request.getHeader("l"), request.getHeader("o"), 
						request.getHeader("cn"), NOPASSWORD, NOPASSWORD, 
						false, request.getHeader("persistent-id").split("!")[2],
						false));
		return "certReq";
	}

	/**
	 * This method receive the user parameter and the password for the proxy
	 * encryption for the MyProxy server where the proxy will be stored and
	 * start the request procedures.
	 * 
	 * @param certificateRequest
	 *            - getting from the form the fields value
	 * @param result
	 *            - validating the {@link CertificateRequest} in result there
	 *            are the errors.
	 * @param model
	 *            - getting the model from the request.
	 * @return the success page if all is ok or return to the form page if some
	 *         problem are occurred.
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
	public final String createCertificateAndProxy(
			@Valid @ModelAttribute("certificateRequest") final CertificateRequest certificateRequest,
			final BindingResult result, final Model model) {
		log.debug("Received request of new Certificate and new Proxy");

		if (result.hasErrors()) {
			model.addAttribute("certificateRequest", certificateRequest);
			log.debug("Not valid request");
			return "home";

		} else {

			GlobusCredential credential = null;

			try {
				credential = RequestCertificateUtil.getCredential(
						certificateRequest, result);
			} catch (Exception e) {
				e.printStackTrace();
				result.reject("Exception", e.getMessage());
				model.addAttribute("certificateRequest", certificateRequest);
				return "home";
			}

			if (credential == null) {
				result.reject("doRequestAndStore.putCertificate",
						"Certificate not stored into MyProxy");
				model.addAttribute("certificateRequest", certificateRequest);
				return "home";
			}

			DBInteracion db = new DBInteracion(voService, userToVoService,
					certificateService, userInfoService);
			String usernameCert = db.insertCertificate(credential,
					certificateRequest.getMail());

			if (usernameCert == null) {
				result.reject("doRequestAndStore.putCertificateIntoDB",
						"Certificate not stored into Portal DB");
				model.addAttribute("certificateRequest", certificateRequest);
				return "home";
			}

			if (!RequestCertificateUtil.putCertificate(credential, result,
					certificateRequest.getProxyPass1(), usernameCert)) {
				result.reject("doRequestAndStore.putCertificate",
						"Certificate not stored into MyProxy");
				model.addAttribute("certificateRequest", certificateRequest);
				return "home";
			}

			if (!VOMSAdminCallOut.putUser(credential.getIdentityCertificate(),
					certificateRequest.getMail(), certificateRequest.getCn())) {
				result.reject("doRequestAndStore.putUserVOMS",
						"User not stored in VOMS");
				model.addAttribute("certificateRequest", certificateRequest);
				return "home";
			}

			db.insertVOMS(certificateRequest.getMail(),
					DNHandler.getSubject(credential.getIdentityCertificate())
							.getX500());
			
			//db.activateUser(certificateRequest.getMail());

			return "success";
		}

	}

	/**
	 * This method receive the user parameter and start the request procedures.
	 * 
	 * @param certificateRequest
	 *            - getting from the form the fields value
	 * @param result
	 *            - validating the {@link CertificateRequest} in result there
	 *            are the errors.
	 * @param model
	 *            - getting the model from the request.
	 * @param response
	 *            - the servlet response.
	 * @param request
	 *            - the servlet request.
	 * @return the success page if all is OK or return to the form page if some
	 *         problem are occurred.
	 * @throws IOException 
	 * @throws NoSuchProviderException 
	 */
	@RequestMapping(value = "/certReq/certReq", method = RequestMethod.POST)
	public final String createCertificate(
			@Valid @ModelAttribute("certificateRequest") final CertificateRequest certificateRequest,
			final BindingResult result, final Model model,
			final HttpServletRequest request, 
			final HttpServletResponse response)
			throws IOException, NoSuchProviderException {
		log.debug("Received request of new Certificate");

		if (result.hasErrors()) {
			model.addAttribute("certificateRequest", certificateRequest);
			log.debug("Not valid request");
			return "certReq";

		} else {

			log.error("Received from certReq: \n"
					+ certificateRequest.toString());

			try {
				X509Certificate cert = null;
				if (request.getHeader("User-Agent").contains("MSIE")) {
					log.debug("spkac=" + request.getParameter("spkac"));
					PKCS10CertificateRequest csr = new PKCS10CertificateRequest(
							certificateRequest);
					cert = csr
							.getX509Certificate(request.getParameter("spkac"));
				} else {
					SpkacCertificateRequest csr = new SpkacCertificateRequest(
							certificateRequest);
					cert = csr
							.getX509Certificate(request.getParameter("spkac"));
				}
				if (cert == null) {
					result.reject("CertificateError",
							"Certificate already requested");
					return "certReq";
				}

				log.debug(cert.getSubjectDN());

				model.addAttribute("dn", cert.getSubjectDN());

				StringWriter sw = new StringWriter();
				PEMWriter pemWriter = new PEMWriter(sw);
				pemWriter.writeObject(cert);
				pemWriter.close();
				String pemCert = sw.toString();

				log.debug(pemCert.trim());

				model.addAttribute("cert", pemCert);
				model.addAttribute("cert2", pemCert.replaceAll("\n", ""));

				model.addAttribute(certificateRequest);
				if (!request.getHeader("User-Agent").contains("MSIE")) {
					FileOutputStream certificate = new FileOutputStream(
							"/etc/pki/tls/certs/CAOnlineBridge/"
									+ certificateRequest.getCn().hashCode()
									+ ".pem");

					if (request.getHeader("User-Agent").contains("Firefox")) {
						PEMWriter pemWriter2 = new PEMWriter(
								new OutputStreamWriter(certificate));
						pemWriter2.writeObject(cert);
						pemWriter2.close();
					} else {
						DEROutputStream derCertificate = new DEROutputStream(
								certificate);
						derCertificate.write(cert.getEncoded());
						derCertificate.close();
					}
				}

				return "successCertReq";

			} catch (Exception e) {
				result.reject("Exception", e.getMessage());
				// e.printStackTrace();
			}

			return "certReq";
		}

	}

	/**
	 * Return the certificate file to the browser.
	 * 
	 * @param fileName
	 *            - the certificate filename.
	 * @param response
	 *            - the servlet response.
	 */
	@RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
	public final void getFile(@PathVariable("file_name") final String fileName,
			final HttpServletResponse response) {

		response.addHeader("Content-Type", "application/x-x509-user-cert");

		File file = new File("/etc/pki/tls/certs/CAOnlineBridge/"
				+ fileName.hashCode() + ".pem");
		FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(file);

			OutputStream out = response.getOutputStream();

			byte[] outputByte = new byte[BYTE_ARRAY_SIZE];
			// copy binary content to output stream
			while (fileIn.read(outputByte, 0, BYTE_ARRAY_SIZE) != -1) {
				out.write(outputByte, 0, BYTE_ARRAY_SIZE);
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
	
	@RequestMapping(value = "/RevokePortal")
	public final String showRevokePortal(final HttpServletRequest request,
			final Model model) {
		String result = showRevoke(request, model);
		if(result.equals("revoke"))
			return "revokePortal";
		return result;
	}
	
	
	@RequestMapping(value = "/Revoke")
	public final String showRevoke(final HttpServletRequest request,
			final Model model) {

		if ((request.getParameter("t1") != null)
				&& (request.getParameter("t2") != null)) {

			log.debug("Token Validation");

			String token1 = request.getParameter("t1");
			String token2 = request.getParameter("t2");

			log.debug("Received t1: " + token1 + " & t2: " + token2);

			String userSecret = request.getHeader("mail");

			String verifyToken = TokenCreator.getToken(userSecret);
			log.debug("Created token: " + verifyToken);

			if ((token1.equals(verifyToken)) || (token2.equals(verifyToken))) {
//				String subject = request.getParameter("subject");
//				String issuer = request.getParameter("issuer");
//				Long snLong = Long.parseLong(request.getParameter("sn")) ;
//				String sn = Long.toHexString(snLong);
				
				
				String mail = request.getHeader("mail");
				log.error(mail);
				DBInteracion db = new DBInteracion(voService, userToVoService,
						certificateService, userInfoService);
				
				Certificate cert = db.getCertificate(mail);
				
				model.addAttribute("revokeRequest", new RevokeRequest(cert.getSubject(), cert.getIssuer(),
						null, RevokedCertInfo.NOT_REVOKED, true, false));
			} else {
				log.error("Intrusion detected from: "
						+ request.getRemoteAddr());
				return "error";
			}
		} else {
			X509Certificate x509[] = (X509Certificate[]) request
					.getAttribute("javax.servlet.request.X509Certificate");
			
			Long l = x509[0].getSerialNumber().longValue();
			String sn = Long.toHexString(l);
			
			model.addAttribute("revokeRequest", new RevokeRequest(x509[0]
					.getSubjectDN().getName(), x509[0].getIssuerDN().getName(),
					sn, RevokedCertInfo.NOT_REVOKED, false,
					false));
		}
		return "revoke";
	}
	
	@RequestMapping(value = "/RevokePortal/certRevoke", method = RequestMethod.POST)
	public String certRevokePortal(@Valid @ModelAttribute("revokeRequest") final RevokeRequest revokeRequest,
			final BindingResult result, final Model model,
			final HttpServletRequest request, 
			final HttpServletResponse response){
		
		String resultPage = certRevoke(revokeRequest, result, model, request, response);
		if(resultPage.equals("revoke"))
			return "revokePortal";
		return resultPage;
	}
	
	@RequestMapping(value = "/Revoke/certRevoke", method = RequestMethod.POST)
	public String certRevoke(@Valid @ModelAttribute("revokeRequest") final RevokeRequest revokeRequest,
			final BindingResult result, final Model model,
			final HttpServletRequest request, 
			final HttpServletResponse response){
		
		log.error(revokeRequest);
		if (result.hasErrors()) {
			
			model.addAttribute("revokeRequest", revokeRequest);
			log.debug("Not valid request");
			
		} else {
			
			try {
				EjbCARevokeRequest rev = new EjbCARevokeRequest();
				rev.revoke(revokeRequest);
				
				//se sono dal portale cancella dal database
				
				if(revokeRequest.isPortalRequest()){
					DBInteracion db = new DBInteracion(voService, userToVoService,
							certificateService, userInfoService);
					db.deleteCert(revokeRequest.getSubjectDN());
				}
				model.addAttribute("revokeRequest", revokeRequest);
				return "revokeSuccess";
			} catch (Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			}
			
		}
		
		return "revoke";
	}
	
	@RequestMapping(value = "/RenewPortal")
	public final String showRenewPortal(final HttpServletRequest request,
			final Model model) {
		String result = showRenew(request, model);
		if(result.equals("renew"))
			return "renewPortal";
		return result;
	}
	
	@RequestMapping(value = "/Renew")
	public final String showRenew(final HttpServletRequest request,
			final Model model) {

		if ((request.getParameter("t1") != null)
				&& (request.getParameter("t2") != null)) {

			log.debug("Token Validation");

			String token1 = request.getParameter("t1");
			String token2 = request.getParameter("t2");

			log.debug("Received t1: " + token1 + " & t2: " + token2);

			String userSecret = request.getHeader("mail");

			String verifyToken = TokenCreator.getToken(userSecret);
			log.debug("Created token: " + verifyToken);

			if ((token1.equals(verifyToken)) || (token2.equals(verifyToken))) {
				
				String mail = request.getHeader("mail");
				log.error(mail);
				DBInteracion db = new DBInteracion(voService, userToVoService,
						certificateService, userInfoService);
				
				Certificate cert = db.getCertificate(mail);
				
		    	Date notAfter = cert.getExpirationDate();
		    	Date check = DateUtil.getDate(MONTH);
				if(notAfter.before(check)){
					model.addAttribute("showRequest", true);
				}else{
					model.addAttribute("showRequest", false);
				}
				
				
				model.addAttribute("revokeRequest", new RevokeRequest(cert.getSubject(), cert.getIssuer(),
						null, RevokedCertInfo.REVOCATION_REASON_UNSPECIFIED, true, true));
				model.addAttribute(
						"certificateRequest",
						new CertificateRequest(request.getHeader("mail"),
								request.getHeader("l"), request.getHeader("o"), 
								request.getHeader("cn"), null, null, false, 
								request.getHeader( "persistent-id").split("!")[2], 
								true));
			} else {
				log.error("Intrusion detected from: "
						+ request.getRemoteAddr());
				return "error";
			}
		} else {
			X509Certificate x509[] = (X509Certificate[]) request
					.getAttribute("javax.servlet.request.X509Certificate");
			
			Long l = x509[0].getSerialNumber().longValue();
			String sn = Long.toHexString(l);
			
			Date notAfter = x509[0].getNotAfter();
			Date check = DateUtil.getDate(MONTH);
			
			log.error(notAfter.toString());
			log.error(check.toString());
			
			if(notAfter.before(check)){
				model.addAttribute("showRequest", true);
			}else{
				model.addAttribute("showRequest", false);
			}
			
			model.addAttribute("revokeRequest", new RevokeRequest(x509[0]
					.getSubjectDN().getName(), x509[0].getIssuerDN().getName(),
					sn, RevokedCertInfo.REVOCATION_REASON_UNSPECIFIED, false,
					true));
			model.addAttribute(
					"certificateRequest",
					new CertificateRequest(request.getHeader("mail"),
							request.getHeader("l"), request.getHeader("o"), 
							request.getHeader("cn"), NOPASSWORD, NOPASSWORD, 
							false, request.getHeader("persistent-id").split("!")[2],
							false));
		}
		return "renew";
	}
	
	@RequestMapping(value = "/RenewPortal/certRenew", method = RequestMethod.POST)
	public String certRenewPortal(@Valid @ModelAttribute("revokeRequest") final RevokeRequest revokeRequest,
			@Valid @ModelAttribute("certificateRequest") final CertificateRequest certificateRequest,
			final BindingResult result, final Model model,
			final HttpServletRequest request, 
			final HttpServletResponse response){
		String resultPage = certRenew(revokeRequest, certificateRequest, result, model, request, response);
		if(resultPage.equals("renew")){
			return "renewPage";
		}
		return resultPage;
	}
	
	@RequestMapping(value = "/Renew/certRenew", method = RequestMethod.POST)
	public String certRenew(@Valid @ModelAttribute("revokeRequest") final RevokeRequest revokeRequest,
			@Valid @ModelAttribute("certificateRequest") final CertificateRequest certificateRequest,
			final BindingResult result, final Model model,
			final HttpServletRequest request, 
			final HttpServletResponse response){
		
		log.error(certificateRequest);
		log.error(revokeRequest);
		if (result.hasErrors()) {
			model.addAttribute("showRequest", true);
			model.addAttribute("revokeRequest", revokeRequest);
			model.addAttribute("certificateRequest", certificateRequest);
			
			List<ObjectError> errors = result.getAllErrors();
			for(ObjectError e: errors){
				log.error(e.toString());
			}
			log.error("Not valid request");
			
		} else {
			
			try {
				
				/* 1 */
				EjbCARevokeRequest rev = new EjbCARevokeRequest();
				rev.revoke(revokeRequest);

				if(revokeRequest.isPortalRequest()){
					/* Richiesta da portale
					 * 
					 * 1 revocare certificate
					 * 2 creare richiesta
					 * 3 richiedere certificato
					 * 4 modificare cert nel db
					 * 5 caricare nel myproxy
					 */
					
					/* 2 3 */
					GlobusCredential credential = RequestCertificateUtil.getCredential(
								certificateRequest, result);

					if (credential == null) {
						result.reject("doRequestAndStore.putCertificate",
								"Certificate not stored into MyProxy");
						model.addAttribute("revokeRequest", revokeRequest);
						model.addAttribute("certificateRequest", certificateRequest);
						return "renew";
					}
					
					/* 4*/
					DBInteracion db = new DBInteracion(voService, userToVoService,
							certificateService, userInfoService);
					String usernameCert = db.updateCertificate(credential,
							certificateRequest.getMail());

					if (usernameCert == null) {
						result.reject("doRequestAndStore.putCertificateIntoDB",
								"Certificate not stored into Portal DB");
						model.addAttribute("revokeRequest", revokeRequest);
						model.addAttribute("certificateRequest", certificateRequest);
						return "renew";
					}

					/* 5 */
					if (!RequestCertificateUtil.putCertificate(credential, result,
							certificateRequest.getProxyPass1(), usernameCert)) {
						result.reject("doRequestAndStore.putCertificate",
								"Certificate not stored into MyProxy");
						model.addAttribute("revokeRequest", revokeRequest);
						model.addAttribute("certificateRequest", certificateRequest);
						return "renew";
					}
					
				}else{
					/* Richiesta da Web 
					 * 
					 * 1 revocare certificate
					 * 2 richiedere nuovo 
					 */
					
					/* 2 */
					
					X509Certificate cert = null;
					if (request.getHeader("User-Agent").contains("MSIE")) {
						log.debug("spkac=" + request.getParameter("spkac"));
						PKCS10CertificateRequest csr = new PKCS10CertificateRequest(
								certificateRequest);
						cert = csr
								.getX509Certificate(request.getParameter("spkac"));
					} else {
						SpkacCertificateRequest csr = new SpkacCertificateRequest(
								certificateRequest);
						cert = csr
								.getX509Certificate(request.getParameter("spkac"));
					}
					if (cert == null) {
						result.reject("CertificateError",
								"Certificate already requested");
						model.addAttribute("revokeRequest", revokeRequest);
						model.addAttribute("certificateRequest", certificateRequest);
						return "renew";
					}

					log.debug(cert.getSubjectDN());

					model.addAttribute("dn", cert.getSubjectDN());

					StringWriter sw = new StringWriter();
					PEMWriter pemWriter = new PEMWriter(sw);
					pemWriter.writeObject(cert);
					pemWriter.close();
					String pemCert = sw.toString();

					log.debug(pemCert.trim());

					model.addAttribute("cert", pemCert);
					model.addAttribute("cert2", pemCert.replaceAll("\n", ""));

					model.addAttribute(certificateRequest);
					if (!request.getHeader("User-Agent").contains("MSIE")) {
						FileOutputStream certificate = new FileOutputStream(
								"/etc/pki/tls/certs/CAOnlineBridge/"
										+ certificateRequest.getCn().hashCode()
										+ ".pem");

						if (request.getHeader("User-Agent").contains("Firefox")) {
							PEMWriter pemWriter2 = new PEMWriter(
									new OutputStreamWriter(certificate));
							pemWriter2.writeObject(cert);
							pemWriter2.close();
						} else {
							DEROutputStream derCertificate = new DEROutputStream(
									certificate);
							derCertificate.write(cert.getEncoded());
							derCertificate.close();
						}
					}

					return "successCertReq";
					
				}
				model.addAttribute("revokeRequest", revokeRequest);
				return "renewSuccess";
			} catch (Exception e) {
				result.reject("Exception", e.getMessage());
				e.printStackTrace();
			}
			
		}
		
		return "renew";
	}
	
	@RequestMapping(value = "/certificate")
	public final String showCertificate(final HttpServletRequest request,
			final Model model) {
		
		
		try{
			
			EjbCAInformationRequest info = new EjbCAInformationRequest();
			model.addAttribute("certList", info.listCertificate(request.getHeader("cn")));
			
		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage());
			e.printStackTrace();
		}
		
		return "certificate";
	}

}
