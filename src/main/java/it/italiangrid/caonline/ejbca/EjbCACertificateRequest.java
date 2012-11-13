package it.italiangrid.caonline.ejbca;

import it.italiangrid.caonline.model.CertificateRequest;

import java.io.IOException;
import java.util.List;

import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;

/**
 * Class that used for submit a certificate request to the EjbCA toward the Web
 * service. Before submitting the request, create a new EjbCA user if is it
 * necessary.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */
public class EjbCACertificateRequest {
	@SuppressWarnings("unused")
	private String mail;
	private String username;
	private String dn;
	protected EjbcaWS service = null;
	protected UserDataVOWS user = null;

	/**
	 * Setter method
	 * 
	 * @param mail
	 */

	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * Setter method
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Setter method
	 * 
	 * @param dn
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * Constructor: create the certificate DN and instantiate the class getting
	 * the information from arguments.
	 * 
	 * @param mail
	 *            - The user's e-mail
	 * @param username
	 *            - The user's username is the CN of the user.
	 * @param dn
	 *            - The certificate DN.
	 */
	public EjbCACertificateRequest(String mail, String username, String dn) {
		super();
		this.mail = mail;
		this.username = username;
		this.dn = dn;

		try {
			EjbcaWSConnection ejbcaWsConn = new EjbcaWSConnection();
			this.service = ejbcaWsConn.getEjbcaWS();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Constructor: create the certificate DN and instantiate the class getting
	 * the information from the CertificateRequest model class.
	 * 
	 * @param mail
	 * @param username
	 * @param dn
	 */
	public EjbCACertificateRequest(CertificateRequest certificateRequest) {
		super();

		String dn = "CN=" + certificateRequest.getCn();
		if (certificateRequest.getL() != null)
			dn += " ,OU=" + certificateRequest.getL();
		if (certificateRequest.getO() != null)
			dn += " ,O=" + certificateRequest.getO();
		dn += ", O=MICS, DC=IGI ,DC=IT";

		this.mail = certificateRequest.getMail();
		this.username = certificateRequest.getCn().trim();
		this.dn = dn;

		try {
			EjbcaWSConnection ejbcaWsConn = new EjbcaWSConnection();
			this.service = ejbcaWsConn.getEjbcaWS();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method that use the EjbCAWS service for adding a new user if dosn't
	 * exists into EjbCA.
	 * 
	 * @throws ApprovalException_Exception
	 * @throws AuthorizationDeniedException_Exception
	 * @throws CADoesntExistsException_Exception
	 * @throws EjbcaException_Exception
	 * @throws UserDoesntFullfillEndEntityProfile_Exception
	 * @throws WaitingForApprovalException_Exception
	 * @throws IllegalQueryException_Exception
	 */
	protected void createEjbcaUser() throws ApprovalException_Exception,
			AuthorizationDeniedException_Exception,
			CADoesntExistsException_Exception, EjbcaException_Exception,
			UserDoesntFullfillEndEntityProfile_Exception,
			WaitingForApprovalException_Exception,
			IllegalQueryException_Exception {

		UserMatch userMatch = new UserMatch(UserMatch.MATCH_WITH_USERNAME,
				UserMatch.MATCH_TYPE_EQUALS, username);

		List<UserDataVOWS> findUsers = service.findUser(userMatch);

		if (findUsers.size() == 0) {
			user = new UserDataVOWS();
			user.setUsername(username);
			user.setSubjectDN(dn);
			user.setCaName("subca-benci");
			user.setEmail(null);
			user.setSubjectAltName(null);
			user.setEndEntityProfileName("benci");
			user.setCertificateProfileName("benci-profile");
			user.setPassword("userTestPasswd");
			user.setClearPwd(false);
			user.setStatus(UserDataVOWS.STATUS_NEW);
			user.setTokenType(UserDataVOWS.TOKEN_TYPE_USERGENERATED);
			service.editUser(user);
		} else {
			user = new UserDataVOWS();
			user = findUsers.get(0);
			user.setPassword("userTestPasswd");
			user.setClearPwd(false);
		}

	}
}