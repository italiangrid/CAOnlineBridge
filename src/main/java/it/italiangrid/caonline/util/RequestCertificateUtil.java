package it.italiangrid.caonline.util;

import it.italiangrid.caonline.model.CertificateRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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

public class RequestCertificateUtil {
	
	/**
	 * Logger of the class DownloadCertificateController. 
	 */
	private static final Logger log = Logger
			.getLogger(RequestCertificateUtil.class);
	

	public static String getCertificate(CertificateRequest certificateRequest, BindingResult result){
		String contextPath = RequestCertificateUtil.class.getClassLoader().getResource("").getPath() + "scriptsCA/";
		
		log.info("Script Location:" + contextPath);
		
		//String cn = "C=IT,O=INFN,L=" + certificateRequest.getL() + ",CN=" + certificateRequest.getCn();
		String cn = "CN=" + certificateRequest.getCn() + ",L=" + certificateRequest.getL() + ",O=INFN,C=IT";
		log.info("CN: "+cn);
		
		String certPath= null;
		
		try {
			
			String[] permissions = new String[]{"chmod", "a+x", contextPath+"pickAcert.sh"};
			Runtime.getRuntime().exec(permissions);
			
			String[] cmd = new String[]{contextPath+"pickAcert.sh", "-S", cn , "-m", certificateRequest.getMail()};
			//String[] cmd = new String[]{contextPath+"pickAcert.sh", "-S", cn};
			
			log.info("cmd = " + cmd);
			
			Process p = Runtime.getRuntime().exec(cmd);
		
			InputStream stdout = p.getInputStream();
			InputStream stderr = p.getErrorStream();
	
			BufferedReader output = new BufferedReader(new InputStreamReader(
					stdout));
			String line = null;
			
			
			
			while ((line = output.readLine()) != null) {
				if(line.contains("path ="))
					certPath = line.replace("path = ", "");
				log.info("[Stdout] " + line);
			}
			output.close();
			
			boolean status = false;
	
			BufferedReader brCleanUp = new BufferedReader(
					new InputStreamReader(stderr));
			while ((line = brCleanUp.readLine()) != null) {
				log.error("[Stderr] " + line);
				status = true;
			}
			
			brCleanUp.close();
			
			if(status){
				result.reject("scriptsCA.pickAcert", "Problem with Certificate Request");
				return null;
			}
			
			log.info("****** CERT PATH: " + certPath + " ******");
		
		} catch (IOException e) {
			result.reject("scriptsCA.pickAcert", "Problem with Certificate Request: " + e.getMessage());
			e.printStackTrace();
		}
		
		return certPath;
		
	}
	
	
	public static GlobusCredential getCredential(CertificateRequest certificateRequest, BindingResult result) throws CertificateException, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, ApprovalException_Exception, UserDoesntFullfillEndEntityProfile_Exception, WaitingForApprovalException_Exception, NoSuchProviderException, IOException{
	
		/*
		 * 1.1 creare coppia chiave privata e pubblica
		 * 1.2 creare pkcs10
		 * 2 inviare richiesta a ejbca
		 * 3 recuperare risposta
		 * 4 convertire in GlobusCredential
		 */
	
		//1.1
		KeyPairGenerator keyGen = null;
		try {
			keyGen= KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		
		keyGen.initialize(2048, new SecureRandom());
		KeyPair keypair = keyGen.generateKeyPair();
		PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();
        
        //1.2
        String sigAlg = "SHA1WithRSA";
        String dn="CN="+certificateRequest.getCn();
		if(certificateRequest.getL()!=null)
			dn += " ,OU="+certificateRequest.getL();
		if(certificateRequest.getO()!=null)
			dn += " ,O="+certificateRequest.getO();
		dn += ", O=MICS, DC=IGI ,DC=IT";
        X509Name subject = new X509Name(dn);
        ASN1Set attributes = new DERSet();
        PKCS10CertificationRequest pkcs10 = null;
        try {
			pkcs10 = new PKCS10CertificationRequest(sigAlg, subject, publicKey, attributes, privateKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		
		//2
		PKCS10CertificateRequest request = new PKCS10CertificateRequest(certificateRequest);
		
		//3
		StringWriter sw = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(sw);
        pemWriter.writeObject(pkcs10);
        pemWriter.close();
        String pemCert = sw.toString();
		log.info("risultato: "+pemCert);
		X509Certificate x509certificate = request.getX509Certificate(pemCert);
		log.info("dn: " + x509certificate.getSubjectDN());
		//4
		X509Certificate[] certs = {x509certificate};
		GlobusCredential credential = new GlobusCredential(privateKey, certs);
		
		return credential;
	}
	
	public static GlobusCredential getCredential(String certPath, BindingResult result){
		
		GlobusCredential credential =null;
		
		try {

			log.info("****** CERT PATH: " + certPath + " ******");
			
			String keyFilePath = certPath+"/theNewCert.p12";
            File keyFile = new File(keyFilePath);
            
            
            
            FileInputStream fstream = new FileInputStream(certPath+"/theNewCertP12pwd");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String password = br.readLine();
            //Close the input stream
            in.close();
            
            log.info("****** theNewCertP12pwd: " + password + " ******");
            
            KeyStore ks = KeyStore.getInstance("PKCS12");
            InputStream iStream = new FileInputStream(keyFile) ;
            ks.load(iStream, password.toCharArray());
            
            Enumeration<String> aliases =ks.aliases();
            String alias = aliases.nextElement();
            
            for (Enumeration<String> e = aliases ; e.hasMoreElements() ;) {
            	log.info(e.nextElement());

            }

            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray())  );
            PrivateKey myPrivateKey = pkEntry.getPrivateKey();
            
            BouncyCastleCertProcessingFactory certFactory = BouncyCastleCertProcessingFactory.getDefault();
            
            java.security.cert.Certificate[] certs = null;
            certs = ks.getCertificateChain(alias);
            X509Certificate[] x509certs = new X509Certificate[certs.length];
            for (int i = 0; i < certs.length; i++) {
                x509certs[i] = certFactory.loadCertificate(
                    new ByteArrayInputStream(certs[i].getEncoded()));
            }
            
            for(int i=0; i< x509certs.length; i++){
	            List<String> listaX509 = x509certs[0].getExtendedKeyUsage();
	            
		        String result2 = "";
		        
		        if(listaX509 != null){
			        for (Iterator<String> iterator = listaX509.iterator(); iterator.hasNext();) {
						String string = (String) iterator.next();
						result2 += string + " | ";
					}
		        }
	        
		        log.info("Usage X509 ["+i+"]: " + result2);
	        
			}
	        log.info("Chains: " + x509certs.length);
            
            credential = new GlobusCredential(myPrivateKey, x509certs);
            
            log.info("Subject = "+credential.getSubject());
            log.info("Subject = "+credential.getTimeLeft());
            credential.getIdentityCertificate().getExtendedKeyUsage();
            
//            X509Certificate userCert;
//            X509Certificate[] userChain;
//            PrivateKey userKey;

//            BouncyCastleOpenSSLKey userKey;
//            
//            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//            
//            KeyStore ks = KeyStore.getInstance( "PKCS12","BC");
//            ks.load( new FileInputStream(keyFile), password.toCharArray() );
//            Enumeration<String> aliases = ks.aliases();
//            
//            Enumeration<String> aliases2 = aliases;
//            
//            while (aliases2.hasMoreElements()) {
//				String string = (String) aliases2.nextElement();
//				log.info("ALIAS: "+ string);
//			}
//            
//            // Take the first alias and hope it is the right one...
//            String alias = (String)aliases.nextElement();
//            
//            log.info("Alias: " + alias);
//            
//            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray())  );
//            PrivateKey myPrivateKey = pkEntry.getPrivateKey();

//            userChain = (X509Certificate[]) ks.getCertificateChain( alias );
//            userCert=(X509Certificate) ks.getCertificate( alias );
//            userKey = new BouncyCastleOpenSSLKey(myPrivateKey);
            
//            VOMSKeyManager vkm = new VOMSKeyManager(keyFilePath, keyFilePath, password, VOMSKeyManager.TYPE_PKCS12);
//            
//            userChain = vkm.getCertificateChain("");
//            userCert=userChain[0];
//            userKey = vkm.getPrivateKey("");
//            
//            
//            for(int i=0; i< userChain.length; i++){
//	            List<String> listaX509 = userChain[i].getExtendedKeyUsage();
//	            
//		        String result2 = "";
//		        
//		        if(listaX509 != null){
//			        for (Iterator<String> iterator = listaX509.iterator(); iterator.hasNext();) {
//						String string = (String) iterator.next();
//						result2 += string + " | ";
//					}
//		        }
//	        
//		        log.info("Usage X509 ["+i+"]: " + result2);
//	        
//			}
//	        log.info("Chains: " + userChain.length);
//            
//            
//            
//            credential = new GlobusCredential(userKey , userChain);
            
            List<String> lista = credential.getIdentityCertificate().getExtendedKeyUsage();
	        String result1 = "";
	        if(lista != null){
	        for (Iterator<String> iterator = lista.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				result1 += string + " | ";
			}
	        }
	        log.info("Usage: " + result1);
            
			return credential;
		
		} catch (IOException e) {
			result.reject("putCertificate.p12", "Problem with Certificate File: " + e.getMessage());
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			result.reject("putCertificate.security", "Problem with Security: " + e.getMessage());
			e.printStackTrace();
		} 
		
		return null;
		
	}
	
	public static boolean putCertificate(GlobusCredential credential, BindingResult result, String password, String usernameCert){
		
		try {
			
            //GSSCredential gssCred = new GlobusGSSCredentialImpl(credential, GSSCredential.INITIATE_AND_ACCEPT);
            
            //log.info("Subject = "+gssCred.getRemainingLifetime());
            
            
            MyProxy myProxyServer = new MyProxy("fullback.cnaf.infn.it",7512);
            
            GSSCredential proxy = createProxy(credential);
            
            String sub = credential.getSubject();
	        String iss = credential.getIssuer();
	        String ide = credential.getIdentity();
	        
	        log.info("credential: " + sub + " " + iss+ "  " + ide);
            
            if(proxy == null){
            	result.reject("putCertificate.create.myproxy", "Problem with MyProxy creation");
            	return false;
            }
            
            log.info("Proxy Subject: " + proxy.getRemainingLifetime());
            
            myProxyServer.put(proxy, usernameCert, password, GSSCredential.DEFAULT_LIFETIME);
            //myProxyServer.put(gssCred, usernameCert, password, GSSCredential.DEFAULT_LIFETIME);

			return true;
		
		} catch (MyProxyException e) {
			result.reject("putCertificate.myproxy", "Problem with MyProxy certificate upload: " + e.getMessage());
			e.printStackTrace();
		} catch (GSSException e) {
			result.reject("putCertificate.gsscredential", "Problem with GSS Credential: " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	private static GSSCredential createProxy(GlobusCredential credential) {
		X509Certificate[] userCert = credential.getCertificateChain();
	    PrivateKey userKey = credential.getPrivateKey();
	    GlobusCredential gridProxy = null;
	    //X509Certificate gridProxy2 = null;
	    try {

	        BouncyCastleCertProcessingFactory factory = 
	            BouncyCastleCertProcessingFactory
	            .getDefault();
	        
	        
	        X509ExtensionSet extSet = new X509ExtensionSet();
	        
	        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
	        
	        
	        X509Extension extension = new X509Extension("2.5.29.15", keyUsage.getEncoded());
			extSet.add(extension);
	        gridProxy = factory.createCredential(userCert , userKey, credential.getStrength(),	0, GSIConstants.GSI_2_PROXY, extSet);
	        //gridProxy2 = factory.createProxyCertificate(credential.getIdentityCertificate(), credential.getPrivateKey(), credential.getIdentityCertificate().getPublicKey(), 0,  GSIConstants.GSI_2_PROXY, extSet, "proxy");
	        
	        String sub = gridProxy.getSubject();
	        String iss = gridProxy.getIssuer();
	        String ide = gridProxy.getIdentity();
	        
//	        List<String> lista = gridProxy.getIdentityCertificate().getExtendedKeyUsage();
//	        String result = "";
//	        for (String string : lista) {
//				result += string + " | ";
//			}
//	        
//	        log.info("Usage: " + result);
	        
	        log.info("Proxy: " + sub + " " + iss+ "  " + ide);
	        
	        File file = new File("/tmp/PROXY");
	        // set read only permissions
	        
	        FileOutputStream out = new FileOutputStream(file);
	        gridProxy.save(out);
	        out.close();
	        
	        return new GlobusGSSCredentialImpl(gridProxy, GSSCredential.INITIATE_AND_ACCEPT);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return null;
	    
	}

//	private boolean insertCertificate(GlobusCredential credential, String mail){
//		
////		UserInfo userInfo = userInfoService.findByMail(mail);
//		UserInfo userInfo = userInfoService.findByUsername("4324127127c021ad8b53ff2b2d38aa5d5944cd65");
//        
//        int uid = userInfo.getUserId();
//       
//        java.security.cert.X509Certificate[] certs = credential.getCertificateChain();
//        
//        java.security.cert.X509Certificate cert = certs[0];
//        
//        Certificate newCertificate = new Certificate(userInfo, credential.getSubject(), cert.getNotAfter(), "true", "true", credential.getIssuer(), null);
//        
//        int id = certificateService.save(newCertificate, uid);
//        
//        if(id!=-1)
//        	return true;
//		
//		return false;
//	}
//	
	public static boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }


}
