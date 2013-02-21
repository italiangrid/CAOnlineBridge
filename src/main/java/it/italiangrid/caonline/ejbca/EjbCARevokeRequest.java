package it.italiangrid.caonline.ejbca;

import it.italiangrid.caonline.model.RevokeRequest;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.ejbca.core.protocol.ws.client.gen.AlreadyRevokedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;

/**
 * 
 * @author dmichelotto
 *
 */
public class EjbCARevokeRequest {
	
	/**
	 * Logger attribute.
	 */
	private static final Logger log = Logger.getLogger(EjbCACertificateRequest.class);
	
	/**
	 * Ejbca Web Service.
	 */
	private EjbcaWS service;

	/**
	 * Contructor
	 * 
	 * @throws EjbCAException
	 */

	public EjbCARevokeRequest() throws EjbCAException {
		log.error("Service initialization...");
		try {
			EjbcaWSConnection ejbcaWsConn = new EjbcaWSConnection();
			this.service = ejbcaWsConn.getEjbcaWS();
			log.error("Service successfully initialized.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new EjbCAException("Connetion problem");
		}
	}

	/**
	 * Revoke the user certificate.
	 * 
	 * @param revokeRequest - the revocation request.
	 * @throws AlreadyRevokedException_Exception 
	 * @throws ApprovalException_Exception 
	 * @throws AuthorizationDeniedException_Exception 
	 * @throws CADoesntExistsException_Exception 
	 * @throws EjbcaException_Exception 
	 * @throws NotFoundException_Exception 
	 * @throws WaitingForApprovalException_Exception 
	 * @throws IllegalQueryException_Exception 
	 * @throws EjbCAException 
	 */
	public void revoke(RevokeRequest revokeRequest)
			throws AlreadyRevokedException_Exception,
			ApprovalException_Exception,
			AuthorizationDeniedException_Exception,
			CADoesntExistsException_Exception, EjbcaException_Exception,
			NotFoundException_Exception, WaitingForApprovalException_Exception, IllegalQueryException_Exception, EjbCAException {
		log.error("Revoking certificate: " + revokeRequest.getSubjectDN());
		String username = "";
		if(revokeRequest.getSubjectDN().contains(","))
			username = revokeRequest.getSubjectDN().split(",")[0].replace("CN=", "");
		if(revokeRequest.getSubjectDN().contains("/")){
			String[] splittedDN = revokeRequest.getSubjectDN().split("/");
			username = splittedDN[splittedDN.length-1].replace("CN=", "");
		}
		
		log.error(username);
		UserMatch userMatch = new UserMatch(UserMatch.MATCH_WITH_USERNAME,
				UserMatch.MATCH_TYPE_EQUALS, username);
		
		List<UserDataVOWS> findUsers = service.findUser(userMatch);
		
		if(findUsers.isEmpty())
			throw new EjbCAException("Certificate not found");
		
		service.revokeUser(findUsers.get(0).getUsername(), revokeRequest.getReason(), true);
		
		
		
		log.error("Certificate successfully revoked");
	}
}
