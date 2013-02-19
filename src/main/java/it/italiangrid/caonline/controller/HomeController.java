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
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.openssl.PEMWriter;
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

import it.italiangrid.caonline.ejbca.PKCS10CertificateRequest;
import it.italiangrid.caonline.ejbca.SpkacCertificateRequest;
import it.italiangrid.caonline.model.CertificateRequest;
import it.italiangrid.caonline.util.DBInteracion;
import it.italiangrid.caonline.util.RequestCertificateUtil;
import it.italiangrid.caonline.util.TokenCreator;
import it.italiangrid.caonline.util.VOMSAdminCallOut;
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
				model.addAttribute("certificateRequest",
						new CertificateRequest(request.getHeader("mail"),
								request.getHeader("cn"),
								request.getHeader("l"), request.getHeader("o"),
								null, null));
				return "home";
			} else {
				log.error("Intrusion detected from: "
						+ request.getRemoteAddr());
				return "error";
			}
		}
		model.addAttribute(
				"certificateRequest",
				new CertificateRequest(request.getHeader("mail"), request
						.getHeader("cn"), request.getHeader("l"), request
						.getHeader("o"), NOPASSWORD, NOPASSWORD));
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

			db.activateUser(certificateRequest.getMail());

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

			log.debug("Received from certReq: \n"
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

}
