package it.italiangrid.caonline.ejbca;

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

import it.italiangrid.caonline.model.CertificateRequest;

/**
 * Class that extend EjbCACertificateRequest class adding a specific method for
 * the PKCS10 certificate request.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */
public class PKCS10CertificateRequest extends EjbCACertificateRequest {

	/**
	 * Constructor
	 * 
	 * @param certificateRequest
	 *            - The user's certificate request model
	 */
	public PKCS10CertificateRequest(CertificateRequest certificateRequest) {
		super(certificateRequest);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get a signed certificate from a PKCS10 CSR.
	 * 
	 * @param pkcs10
	 *            - The CSR in PKCS10 format.
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
	public X509Certificate getX509Certificate(String pkcs10)
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
			CertificateResponse certenv = service.pkcs10Request(
					user.getUsername(), user.getPassword(), pkcs10, null,
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

		/*
		 * if ((user) == null) return null;
		 * 
		 * CertificateResponse certenv =
		 * service.pkcs10Request(user.getUsername(), user.getPassword(), pkcs10,
		 * null, CertificateHelper.RESPONSETYPE_CERTIFICATE);
		 * 
		 * X509Certificate cert = certenv.getCertificate();
		 * 
		 * return cert;
		 */
	}

}
