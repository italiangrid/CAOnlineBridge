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

/**
 * Class that provide the VOMSAdmin web service connection and an method that
 * add user into specified VOMS.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */

public class VOMSAdminCallOut {
	/*
	 * Eclipse configuration for test.
	 * -Daxis.socketSecureFactory=org.glite.security
	 * .trustmanager.axis.AXISSocketFactory
	 * -DsslCertFile=/etc/grid-security/hostcert.pem
	 * -DsslKey=/etc/grid-security/hostkey.pem
	 */

	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(VOMSAdminCallOut.class);

	/**
	 * Default configuration if the configuration file dosn't exist.
	 */
	private static final String DEFAULT_SSL_CERT_FILE = "/etc/grid-security/hostcert.pem";
	private static final String DEFAULT_SSL_KEY = "/etc/grid-security/hostkey.pem";
	private static final String VOMS_HOST = "gridlab11.cnaf.infn.it";
	private static final String VO = "vomstest";

	/**
	 * Call the VOMSAdmin web service for adding a user into the specified VOMS.
	 * 
	 * @param credential
	 *            - the user certificate
	 * @param mail
	 *            - the user e-mail address
	 * @param cn
	 *            - the user CN
	 * @return true if all went done
	 */
	public static boolean putUser(X509Certificate credential, String mail,
			String cn) {

		String cert = DEFAULT_SSL_CERT_FILE;
		String key = DEFAULT_SSL_KEY;
		String host = VOMS_HOST;
		String vo = VO;

		String contextPath = VOMSAdminCallOut.class.getClassLoader()
				.getResource("").getPath();

		log.debug("Context Path:" + contextPath);

		File test = new File(contextPath + "/CAOnlineBridge.properties");
		log.debug("File: " + test.getAbsolutePath());
		if (test.exists()) {
			log.debug("Configuration file exist");
			try {
				FileInputStream inStream = new FileInputStream(contextPath
						+ "/CAOnlineBridge.properties");

				Properties prop = new Properties();

				prop.load(inStream);

				inStream.close();

				cert = prop.getProperty("SSL_CERT_FILE");
				key = prop.getProperty("SSL_KEY");
				host = prop.getProperty("VOMS_HOST");
				vo = prop.getProperty("VO");

				log.debug("try cert: " + cert + " key: " + key);
			} catch (IOException e) {
				log.error("Certificate problem: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			log.debug("File " + test.toString()
					+ "not found. Using default settings.");
		}

		if (AxisProperties.getProperty("axis.socketSecureFactory") == null) {
			AxisProperties.setProperty("axis.socketSecureFactory",
					"org.glite.security.trustmanager.axis.AXISSocketFactory");
		}

		log.debug("Porperties setted for axis: "
				+ AxisProperties.getProperty("axis.socketSecureFactory")
						.toString());

		Properties properties = AXISSocketFactory.getCurrentProperties();

		log.debug("Setted cert: " + cert + " key: " + key);

		properties.setProperty("sslCertFile", cert);
		properties.setProperty("sslKey", key);

		AXISSocketFactory.setCurrentProperties(properties);
		System.setProperties(properties);

		Properties properties2 = AXISSocketFactory.getCurrentProperties();

		log.debug("AXIS cert: " + properties2.getProperty("sslCertFile"));
		log.debug("AXIS key: " + properties2.getProperty("sslKey"));

		try {

			log.debug("Contact VOMSAdmin with URL = https://" + host
					+ ":8443/voms/" + vo + "services/VOMSAdmin");

			String url = "https://" + host + ":8443/voms/" + vo
					+ "/services/VOMSAdmin";

			log.debug("Finding VOMSAdmin Service: " + url);

			VOMSAdminServiceLocator locator = new VOMSAdminServiceLocator();

			log.debug("Getting VOMSAdmin Service");

			VOMSAdmin adminService;

			URL vomsUrl = new URL(url);

			log.debug("Protocol: " + vomsUrl.getProtocol() + " Host: "
					+ vomsUrl.getHost() + " Port: " + vomsUrl.getPort()
					+ " Path: " + vomsUrl.getPath());

			adminService = locator.getVOMSAdmin(vomsUrl);

			User user = new User();

			user.setCA(DNHandler.getIssuer(credential).getX500());
			log.debug("Issuer: " + DNHandler.getIssuer(credential).getX500());

			user.setDN(DNHandler.getSubject(credential).getX500());
			log.debug("Subject: " + DNHandler.getSubject(credential).getX500());

			user.setCertUri("");
			user.setCN(cn);
			user.setMail(mail);

			log.debug("VO: " + adminService.getVOName());

			if (adminService.getUser(user.getDN(), user.getCA()) == null)
				adminService.createUser(user);

			return true;

		} catch (VOMSException e) {
			log.error("VOMSexception " + e.getMessage());
			e.printStackTrace();
		} catch (RemoteException e) {
			log.error("RemoteException " + e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			log.error("MalformedURLException " + e.getMessage());
			e.printStackTrace();
		} catch (ServiceException e) {
			log.error("ServiceException " + e.getMessage());
			e.printStackTrace();
		} finally {
			properties.remove("sslCertFile");
			properties.remove("sslKey");
		}

		return false;

	}

}
