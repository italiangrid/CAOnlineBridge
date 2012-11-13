package it.italiangrid.caonline.ejbca;

import it.italiangrid.caonline.model.CertificateRequest;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.ejbca.core.protocol.ws.common.CertificateHelper;

/**
 * Class that extend EjbCACertificateRequest class adding a specific method for
 * the spkac certificate request.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */
public class SpkacCertificateRequest extends EjbCACertificateRequest {

	/**
	 * Constructor
	 * 
	 * @param certificateRequest
	 *            - The user's certificate request model
	 */
	public SpkacCertificateRequest(CertificateRequest certificateRequest) {
		super(certificateRequest);
	}

	/**
	 * Get a signed certificate from a spkac CSR.
	 * 
	 * @param spkac
	 *            - The CSR in spkac format.
	 * @return The user's certificate.
	 * @throws AuthorizationDeniedException_Exception
	 * @throws CADoesntExistsException_Exception
	 * @throws EjbcaException_Exception
	 * @throws NotFoundException_Exception
	 * @throws CertificateException
	 * @throws WaitingForApprovalException_Exception
	 * @throws UserDoesntFullfillEndEntityProfile_Exception
	 * @throws ApprovalException_Exception
	 * @throws IllegalQueryException_Exception
	 * @throws EjbCAException
	 */
	public X509Certificate getX509Certificate(String spkac)
			throws AuthorizationDeniedException_Exception,
			CADoesntExistsException_Exception, EjbcaException_Exception,
			NotFoundException_Exception, CertificateException,
			ApprovalException_Exception,
			UserDoesntFullfillEndEntityProfile_Exception,
			WaitingForApprovalException_Exception,
			IllegalQueryException_Exception, EjbCAException {

		createEjbcaUser();

		if ((user) == null) {
			throw new EjbCAException("User not created");
		}
		switch (user.getStatus()) {

		case UserDataVOWS.STATUS_NEW:
			CertificateResponse certenv = service.spkacRequest(
					user.getUsername(), user.getPassword(), spkac, null,
					CertificateHelper.RESPONSETYPE_CERTIFICATE);

			if (certenv == null) {
				throw new EjbCAException("Certificate not created");
			}

			X509Certificate cert = certenv.getCertificate();

			return cert;

		case UserDataVOWS.STATUS_GENERATED:
			throw new EjbCAException("Certificate already generated");

		default:
			throw new EjbCAException("User Problem");
		}
	}

}
