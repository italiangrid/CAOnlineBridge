package it.italiangrid.caonline.util;

import it.italiangrid.caonline.model.CertificateRequest;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.ejbca.core.protocol.ws.common.CertificateHelper;

public class SpkacCertificateRequest extends EjbCACertificateRequest{
	

	public SpkacCertificateRequest(CertificateRequest certificateRequest) {
		super(certificateRequest);
	}

	/**
	 * Get
	 * 
	 * @param spkac
	 * @return
	 * @throws AuthorizationDeniedException_Exception
	 * @throws CADoesntExistsException_Exception
	 * @throws EjbcaException_Exception
	 * @throws NotFoundException_Exception
	 * @throws CertificateException
	 * @throws WaitingForApprovalException_Exception
	 * @throws UserDoesntFullfillEndEntityProfile_Exception
	 * @throws ApprovalException_Exception
	 */
	public X509Certificate getX509Certificate(String spkac)
			throws AuthorizationDeniedException_Exception,
			CADoesntExistsException_Exception, EjbcaException_Exception,
			NotFoundException_Exception, CertificateException,
			ApprovalException_Exception,
			UserDoesntFullfillEndEntityProfile_Exception,
			WaitingForApprovalException_Exception {

		createEjbcaUser();

		if ((user) == null)
			return null;

		CertificateResponse certenv = service.spkacRequest(user.getUsername(),
				user.getPassword(), spkac, null,
				CertificateHelper.RESPONSETYPE_CERTIFICATE);

		X509Certificate cert = certenv.getCertificate();

		return cert;
	}

	

}
