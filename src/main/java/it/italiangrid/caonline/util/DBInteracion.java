package it.italiangrid.caonline.util;

import it.italiangrid.portal.dbapi.domain.Certificate;
import it.italiangrid.portal.dbapi.domain.UserInfo;
import it.italiangrid.portal.dbapi.domain.UserToVo;
import it.italiangrid.portal.dbapi.domain.Vo;
import it.italiangrid.portal.dbapi.services.CertificateService;
import it.italiangrid.portal.dbapi.services.UserInfoService;
import it.italiangrid.portal.dbapi.services.UserToVoService;
import it.italiangrid.portal.dbapi.services.VoService;

import java.util.List;

import org.apache.log4j.Logger;
import org.glite.security.util.DNHandler;
import org.globus.gsi.GlobusCredential;

/**
 * Class that provide the Portal database connection for associte certificate
 * and voms attributes for a specific user.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */

public class DBInteracion {

	/**
	 * Logger of the class RequestCertificateUtil.
	 */
	private static final Logger log = Logger.getLogger(DBInteracion.class);

	private VoService voService;

	private UserToVoService userToVoService;

	private CertificateService certificateService;

	private UserInfoService userInfoService;

	/**
	 * Constructor
	 * 
	 * @param voService
	 * @param userToVoService
	 * @param certificateService
	 * @param userInfoService
	 */
	public DBInteracion(VoService voService, UserToVoService userToVoService,
			CertificateService certificateService,
			UserInfoService userInfoService) {
		super();
		this.voService = voService;
		this.userToVoService = userToVoService;
		this.certificateService = certificateService;
		this.userInfoService = userInfoService;
	}

	/**
	 * Method for insert certificate information of the user's certificate into
	 * portal DB
	 * 
	 * @param credential
	 *            - User's certificate
	 * @param mail
	 *            - User's e-mail for identification.
	 * @return return the username associated to the certificate in the portal
	 *         DB
	 */
	public String insertCertificate(GlobusCredential credential, String mail) {

		UserInfo userInfo = userInfoService.findByMail(mail);

		int uid = userInfo.getUserId();

		java.security.cert.X509Certificate[] certs = credential
				.getCertificateChain();

		java.security.cert.X509Certificate cert = certs[0];

		List<Certificate> certificates = certificateService.findById(uid);

		log.info("/" + credential.getSubject().replaceAll(", ", "/"));

		Certificate newCertificate = new Certificate(userInfo, DNHandler
				.getSubject(cert).getX500(), cert.getNotAfter(), "true",
				certificates.isEmpty() ? "true" : "false", DNHandler.getIssuer(
						credential.getIdentityCertificate()).getX500(), null);

		int id = certificateService.save(newCertificate, uid);

		if (id != -1)
			return certificateService.findByIdCert(id).getUsernameCert();

		return null;
	}

	/**
	 * Method that activate user into the portal for using the grid
	 * 
	 * @param mail
	 *            - User's e-mail for identification.
	 * @return return false.
	 */
	public boolean activateUser(String mail) {

		UserInfo userInfo = userInfoService.findByMail(mail);

		userInfo.setRegistrationComplete("true");

		userInfoService.save(userInfo);

		return false;
	}

	/**
	 * Method that associate VO at the user.
	 * 
	 * @param mail
	 *            - User's e-mail for identification.
	 * @param subject
	 *            - Certificate's DN
	 */
	public void insertVOMS(String mail, String subject) {

		UserInfo userInfo = userInfoService.findByMail(mail);

		Vo vo = voService.findByName("vomstest");

		log.debug("Vo = " + vo.getVo() + " : " + vo.getIdVo());
		log.debug("Subject: " + subject);
		userToVoService.save(userInfo.getUserId(), vo.getIdVo(), subject);

		List<UserToVo> utvo = userToVoService.findById(userInfo.getUserId());

		if (utvo.size() == 1)
			userToVoService.setDefault(userInfo.getUserId(), vo.getIdVo());

	}

}
