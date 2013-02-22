package it.italiangrid.caonline.ejbca;

import it.italiangrid.caonline.model.CertificateInformation;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.Certificate;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;

/**
 * 
 * @author dmichelotto
 *
 */
public class EjbCAInformationRequest {
	
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

	public EjbCAInformationRequest() throws EjbCAException {
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
	 * List user certificates.
	 * @param username - to search
	 * @return list of certificate
	 * @throws AuthorizationDeniedException_Exception
	 * @throws EjbcaException_Exception
	 * @throws IllegalQueryException_Exception
	 * @throws CertificateException 
	 */
	public List<CertificateInformation> listCertificate(String username) throws AuthorizationDeniedException_Exception, EjbcaException_Exception, IllegalQueryException_Exception, CertificateException{
		List<CertificateInformation> result = new ArrayList<CertificateInformation>();
		
		UserMatch userMatch = new UserMatch(UserMatch.MATCH_WITH_USERNAME,
				UserMatch.MATCH_TYPE_CONTAINS, username);
		
		List<UserDataVOWS> findUsers = service.findUser(userMatch);
		
		for(UserDataVOWS user : findUsers){
			List<Certificate> certs = service.findCerts(user.getUsername(), true);
			for(Certificate cert :certs){
				result.add(new CertificateInformation(user.getCertificateProfileName(), cert));
			}
		}
		
		return result;
	}
}
