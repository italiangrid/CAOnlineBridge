package it.italiangrid.caonline.util;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.globus.gsi.GlobusCredential;

import it.italiangrid.caonline.model.CertificateRequest;

public class PKCS10Test {

	/**
	 * @param args
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
	 */
	public static void main(String[] args) throws CertificateException, NoSuchProviderException, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, ApprovalException_Exception, UserDoesntFullfillEndEntityProfile_Exception, WaitingForApprovalException_Exception, IOException {
		// TODO Auto-generated method stub
		CertificateRequest cr = new CertificateRequest("pluto@paperino.it", "CN=Diego Michelotto,OU=cnaf,O=Istituto Nazionale di Fisica Nucleare,O=MICS,DC=IGI,DC=IT ", "CNAF", "INFN", "pippo", "pippo");
		
		GlobusCredential gc = RequestCertificateUtil.getCredential(cr, null);
		
		System.out.println(gc.getSubject());
	}

}
