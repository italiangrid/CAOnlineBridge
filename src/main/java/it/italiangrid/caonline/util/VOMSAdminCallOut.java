package it.italiangrid.caonline.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;
import java.security.cert.X509Certificate;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisProperties;
import org.apache.log4j.Logger;
import org.glite.security.trustmanager.axis.AXISSocketFactory;
import org.glite.security.util.DNHandler;
import org.glite.security.voms.User;
import org.glite.security.voms.VOMSException;
import org.glite.security.voms.service.admin.VOMSAdmin;
import org.glite.security.voms.service.admin.VOMSAdminServiceLocator;

public class VOMSAdminCallOut {

	// -Daxis.socketSecureFactory=org.glite.security.trustmanager.axis.AXISSocketFactory
	// -DsslCertFile=/etc/grid-security/hostcert.pem
	// -DsslKey=/etc/grid-security/hostkey.pem

	private static final Logger log = Logger.getLogger(VOMSAdminCallOut.class);

	private static final String DEFAULT_SSL_CERT_FILE = "/etc/grid-security/hostcert.pem";
	private static final String DEFAULT_SSL_KEY = "/etc/grid-security/hostkey.pem";
	private static final String VOMS_HOST = "gridlab11.cnaf.infn.it";
	private static final String VO = "vomstest";

	public static boolean putUser(X509Certificate credential, String mail, String cn) {
		
		String cert = DEFAULT_SSL_CERT_FILE;
		String key = DEFAULT_SSL_KEY;
		String host = VOMS_HOST;
		String vo = VO;

		String contextPath = VOMSAdminCallOut.class.getClassLoader().getResource("").getPath();
		
		log.info("dove sono:" + contextPath);
		
		File test = new File(contextPath + "/CAOnlineBridge.properties");
		log.info("File: " + test.getAbsolutePath());
		if(test.exists()){
			log.info("ESISTE!!");
			try {
				FileInputStream inStream =
			    new FileInputStream(contextPath + "/CAOnlineBridge.properties");
		
				Properties prop = new Properties();
			
				prop.load(inStream);
			
				inStream.close();
				
				cert = prop.getProperty("SSL_CERT_FILE");
				key = prop.getProperty("SSL_KEY");
				host = prop.getProperty("VOMS_HOST");
				vo = prop.getProperty("VO");
				
				log.info("try cert: "+ cert + " key: "+key);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			log.debug("File "+test.toString()+"not found. Using default settings.");
		}
		

		if(AxisProperties.getProperty("axis.socketSecureFactory") == null){
			AxisProperties.setProperty("axis.socketSecureFactory",
			"org.glite.security.trustmanager.axis.AXISSocketFactory");
		}

		log.info("porpietˆ axis: " + AxisProperties.getProperty("axis.socketSecureFactory").toString());
		
		Properties properties = AXISSocketFactory.getCurrentProperties();

		// log.info(properties);

		// Properties old = AXISSocketFactory.getCurrentProperties();

		// log.info(old);

		log.info("set cert: "+ cert + " key: "+key);

		properties.setProperty("sslCertFile", cert); //
		// hostcert.pem

		properties.setProperty("sslKey", key); // hostkey.pem

		AXISSocketFactory.setCurrentProperties(properties);
		System.setProperties(properties);

		/*
		 * if(properties.equals(old)){ log.info("***** UGUALI *****"); } else {
		 * log.info("***** DIVERSI *****"); }
		 */

		Properties properties2 = AXISSocketFactory.getCurrentProperties();

		// log.info(properties2);
		
		log.info("AXIS cert: " + properties2.getProperty("sslCertFile"));
		log.info("AXIS key: " + properties2.getProperty("sslKey"));
		
		

		try {
			

			log.info("Contatto VOMSAdmin con URL = https://" + host
					+ ":8443/voms/"+vo+"services/VOMSAdmin");

			String url = "https://" + host + ":8443/voms/"+vo+"/services/VOMSAdmin";

			log.info("Trovo VOMSAdmin Service: "+ url);

			VOMSAdminServiceLocator locator = new VOMSAdminServiceLocator();

			log.info("Prendo VOMSAdmin Service");

			VOMSAdmin adminService;
			
			URL vomsUrl = new URL(url);
			
			log.info("Protocol: " + vomsUrl.getProtocol() + " Host: " + vomsUrl.getHost() + " Port: " + vomsUrl.getPort() + " Path: " + vomsUrl.getPath());

			adminService = locator.getVOMSAdmin(vomsUrl);
			
			User user = new User();
			
			user.setCA(DNHandler.getIssuer(credential).getX500());
			log.info("Issuer: "+ DNHandler.getIssuer(credential).getX500());
			
			user.setDN(DNHandler.getSubject(credential).getX500());
			log.info("Subject: "+ DNHandler.getSubject(credential).getX500());
			
			user.setCertUri("");
			user.setCN(cn);
			user.setMail(mail);

			log.info("VO: " + adminService.getVOName());
			
			
			if(adminService.getUser(user.getDN(), user.getCA())==null)
				adminService.createUser(user);
			
			return true;

		} catch (VOMSException e) {
			log.info("VOMSexception");
			e.printStackTrace();

		} catch (RemoteException e) {
			log.info("RemoteException");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			log.info("MalformedURLException");
			e.printStackTrace();
		} catch (ServiceException e) {
			log.info("ServiceException");
			e.printStackTrace();
		} finally {
			properties.remove("sslCertFile");
			properties.remove("sslKey");
		}

		return false;

	}

	public static void test() {
		try {
			
			String cert = DEFAULT_SSL_CERT_FILE;
			String key = DEFAULT_SSL_KEY;
			
			String contextPath = VOMSAdminCallOut.class.getClassLoader().getResource("").getPath();
			
			log.info("dove sono:" + contextPath);
			
			File test = new File(contextPath + "/CAOnlineBridge.properties");
			log.info("File: " + test.getAbsolutePath());
			if(test.exists()){
				log.info("ESISTE!!");
				try {
					FileInputStream inStream =
				    new FileInputStream(contextPath + "/CAOnlineBridge.properties");
			
					Properties prop = new Properties();
				
					prop.load(inStream);
				
					inStream.close();
					
					cert = prop.getProperty("SSL_CERT_FILE");
					key = prop.getProperty("SSL_KEY");
					
					log.info("try cert: "+ cert + " key: "+key);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				log.debug("File "+test.toString()+"not found. Using default settings.");
			}
			
			
			
			if(AxisProperties.getProperty("axis.socketSecureFactory") == null){
				AxisProperties.setProperty("axis.socketSecureFactory",
				"org.glite.security.trustmanager.axis.AXISSocketFactory");
			}

			log.info("porpietˆ axis: " + AxisProperties.getProperty("axis.socketSecureFactory").toString());
			
			Properties properties = AXISSocketFactory.getCurrentProperties();

			// log.info(properties);

			// Properties old = AXISSocketFactory.getCurrentProperties();

			// log.info(old);

			log.info("set cert: "+ cert + " key: "+key);

			properties.setProperty("sslCertFile", cert); //
			// hostcert.pem

			properties.setProperty("sslKey", key); // hostkey.pem

			AXISSocketFactory.setCurrentProperties(properties);
			System.setProperties(properties);

			/*
			 * if(properties.equals(old)){ log.info("***** UGUALI *****"); } else {
			 * log.info("***** DIVERSI *****"); }
			 */

			Properties properties2 = AXISSocketFactory.getCurrentProperties();

			// log.info(properties2);
			
			log.info("AXIS cert: " + properties2.getProperty("sslCertFile"));
			log.info("AXIS key: " + properties2.getProperty("sslKey"));
			

			log.info("Contatto VOMSAdmin con URL = https://voms.cnaf.infn.it:8443/voms/gridit/services/VOMSAdmin");

			String url = "https://voms.cnaf.infn.it:8443/voms/gridit/services/VOMSAdmin";

			log.info("Trovo VOMSAdmin Service: "+ url);

			VOMSAdminServiceLocator locator = new VOMSAdminServiceLocator();

			log.info("Prendo VOMSAdmin Service");

			VOMSAdmin adminService;
			
			URL vomsUrl = new URL(url);
			
			log.info("Protocol: " + vomsUrl.getProtocol() + " Host: " + vomsUrl.getHost() + " Port: " + vomsUrl.getPort() + " Path: " + vomsUrl.getPath());

			adminService = locator.getVOMSAdmin(vomsUrl);

			log.info("TEST: " + adminService.getVOName());

		} catch (VOMSException e) {
			log.info("VOMSexception");
			e.printStackTrace();

		} catch (RemoteException e) {
			log.info("RemoteException");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			log.info("MalformedURLException");
			e.printStackTrace();
		} catch (ServiceException e) {
			log.info("ServiceException");
			e.printStackTrace();
		}
		
	}
}
