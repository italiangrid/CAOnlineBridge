package it.italiangrid.caonline.util;

import it.italiangrid.caonline.ejbca.EjbCAException;
import it.italiangrid.caonline.ejbca.PKCS10CertificateRequest;
import it.italiangrid.caonline.model.CertificateRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.openssl.PEMWriter;
import org.ejbca.core.protocol.ws.client.gen.ApprovalException_Exception;
import org.ejbca.core.protocol.ws.client.gen.AuthorizationDeniedException_Exception;
import org.ejbca.core.protocol.ws.client.gen.CADoesntExistsException_Exception;
import org.ejbca.core.protocol.ws.client.gen.EjbcaException_Exception;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.NotFoundException_Exception;
import org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile_Exception;
import org.ejbca.core.protocol.ws.client.gen.WaitingForApprovalException_Exception;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.X509Extension;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;

/**
 * Class that define some utility method for the Controller
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */
public class RequestCertificateUtil {

	/**
	 * Logger of the class RequestCertificateUtil.
	 */
	private static final Logger log = Logger
			.getLogger(RequestCertificateUtil.class);

	/**
	 * Creation of an CSR, CSR signed by the Online CA and return the user
	 * certificate.
	 * 
	 * @param certificateRequest
	 *            - the request of the certificate
	 * @param result
	 *            - object that contains the result of the operation
	 * @return The User Certificate
	 * @throws CertificateException
	 * @throws AuthorizationDeniedException_Exception
	 * @throws CADoesntExistsException_Exception
	 * @throws EjbcaException_Exception
	 * @throws NotFoundException_Exception
	 * @throws ApprovalException_Exception
	 * @throws UserDoesntFullfillEndEntityProfile_Exception
	 * @throws WaitingForApprovalException_Exception
	 * @throws NoSuchProviderException
	 * @throws IOException
	 * @throws IllegalQueryException_Exception
	 * @throws EjbCAException
	 */
	public static GlobusCredential getCredential(
			CertificateRequest certificateRequest, BindingResult result)
			throws CertificateException,
			AuthorizationDeniedException_Exception,
			CADoesntExistsException_Exception, EjbcaException_Exception,
			NotFoundException_Exception, ApprovalException_Exception,
			UserDoesntFullfillEndEntityProfile_Exception,
			WaitingForApprovalException_Exception, NoSuchProviderException,
			IOException, IllegalQueryException_Exception, EjbCAException {

		/*
		 * Creation of key pair
		 */
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		keyGen.initialize(2048, new SecureRandom());
		KeyPair keypair = keyGen.generateKeyPair();
		PublicKey publicKey = keypair.getPublic();
		PrivateKey privateKey = keypair.getPrivate();

		/*
		 * Creation of CSR
		 */
		String sigAlg = "SHA1WithRSA";
		String dn = "CN=" + certificateRequest.getCn();
		if (certificateRequest.getL() != null)
			dn += " ,OU=" + certificateRequest.getL();
		if (certificateRequest.getO() != null)
			dn += " ,O=" + certificateRequest.getO();
		dn += ", O=MICS, DC=IGI ,DC=IT";
		X509Name subject = new X509Name(dn);
		ASN1Set attributes = new DERSet();
		PKCS10CertificationRequest pkcs10 = null;
		try {
			pkcs10 = new PKCS10CertificationRequest(sigAlg, subject, publicKey,
					attributes, privateKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}

		/*
		 * Request signature
		 */
		PKCS10CertificateRequest request = new PKCS10CertificateRequest(
				certificateRequest);

		/*
		 * Get ejbca response
		 */
		StringWriter sw = new StringWriter();
		PEMWriter pemWriter = new PEMWriter(sw);
		pemWriter.writeObject(pkcs10);
		pemWriter.close();
		String pemCert = sw.toString();
		log.debug("result: " + pemCert);
		X509Certificate x509certificate = request.getX509Certificate(pemCert);
		log.debug("dn: " + x509certificate.getSubjectDN());

		/*
		 * GlobusCredential conversion
		 */
		X509Certificate[] certs = { x509certificate };
		GlobusCredential credential = new GlobusCredential(privateKey, certs);

		return credential;
	}

	public static boolean putCertificate(GlobusCredential credential,
			BindingResult result, String password, String usernameCert) {

		try {

			MyProxy myProxyServer = new MyProxy("fullback.cnaf.infn.it", 7512);

			GSSCredential proxy = createProxy(credential);

			String sub = credential.getSubject();
			String iss = credential.getIssuer();
			String ide = credential.getIdentity();

			log.debug("credential: " + sub + " " + iss + "  " + ide);

			if (proxy == null) {
				result.reject("putCertificate.create.myproxy",
						"Problem with MyProxy creation");
				return false;
			}

			log.debug("Proxy Subject: " + proxy.getRemainingLifetime());

			myProxyServer.put(proxy, usernameCert, password,
					GSSCredential.DEFAULT_LIFETIME);

			return true;

		} catch (MyProxyException e) {
			result.reject(
					"putCertificate.myproxy",
					"Problem with MyProxy certificate upload: "
							+ e.getMessage());
			log.debug("Problem with MyProxy certificate upload: "
					+ e.getMessage());
			e.printStackTrace();
		} catch (GSSException e) {
			result.reject("putCertificate.gsscredential",
					"Problem with GSS Credential: " + e.getMessage());
			log.debug("Problem with GSS Credential: " + e.getMessage());
			e.printStackTrace();
		}

		return false;

	}

	private static GSSCredential createProxy(GlobusCredential credential) {
		X509Certificate[] userCert = credential.getCertificateChain();
		PrivateKey userKey = credential.getPrivateKey();
		GlobusCredential gridProxy = null;
		try {

			BouncyCastleCertProcessingFactory factory = BouncyCastleCertProcessingFactory
					.getDefault();

			X509ExtensionSet extSet = new X509ExtensionSet();

			KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature
					| KeyUsage.keyEncipherment);

			X509Extension extension = new X509Extension("2.5.29.15",
					keyUsage.getEncoded());
			extSet.add(extension);
			gridProxy = factory.createCredential(userCert, userKey,
					credential.getStrength(), 0, GSIConstants.GSI_2_PROXY,
					extSet);

			String sub = gridProxy.getSubject();
			String iss = gridProxy.getIssuer();
			String ide = gridProxy.getIdentity();

			log.info("Proxy: " + sub + " " + iss + "  " + ide);

			File file = new File("/tmp/PROXY");

			FileOutputStream out = new FileOutputStream(file);
			gridProxy.save(out);
			out.close();

			return new GlobusGSSCredentialImpl(gridProxy,
					GSSCredential.INITIATE_AND_ACCEPT);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

}
