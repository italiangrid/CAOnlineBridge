package it.italiangrid.caonline.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisProperties;
import org.apache.log4j.Logger;
import org.glite.security.trustmanager.axis.AXISSocketFactory;
import org.glite.security.voms.VOMSException;
import org.glite.security.voms.service.admin.VOMSAdmin;
import org.glite.security.voms.service.admin.VOMSAdminServiceLocator;

/**
 * Test class for the VOMSAdmin web service
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */

public class VOMSTest {
	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(VOMSTest.class);

	/**
	 * Default configuration if the configuration file dosn't exist.
	 */
	private static final String DEFAULT_SSL_CERT_FILE = "/etc/grid-security/hostcert.pem";
	private static final String DEFAULT_SSL_KEY = "/etc/grid-security/hostkey.pem";

	/**
	 * Main function for local test.
	 * 
	 * @param args
	 * @throws VOMSException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public static void main(String[] args) throws VOMSException,
			MalformedURLException, RemoteException, ServiceException {

		test();

	}

	/**
	 * Test class
	 */
	public static void test() {
		try {

			String cert = DEFAULT_SSL_CERT_FILE;
			String key = DEFAULT_SSL_KEY;

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
				AxisProperties
						.setProperty("axis.socketSecureFactory",
								"org.glite.security.trustmanager.axis.AXISSocketFactory");
			}

			log.debug("Properties setted for axis: "
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

			log.debug("Contact VOMSAdmin with URL = https://voms.cnaf.infn.it:8443/voms/gridit/services/VOMSAdmin");

			String url = "https://voms.cnaf.infn.it:8443/voms/gridit/services/VOMSAdmin";

			log.debug("Finding VOMSAdmin Service: " + url);

			VOMSAdminServiceLocator locator = new VOMSAdminServiceLocator();

			log.debug("Getting VOMSAdmin Service");

			VOMSAdmin adminService;

			URL vomsUrl = new URL(url);

			log.debug("Protocol: " + vomsUrl.getProtocol() + " Host: "
					+ vomsUrl.getHost() + " Port: " + vomsUrl.getPort()
					+ " Path: " + vomsUrl.getPath());

			adminService = locator.getVOMSAdmin(vomsUrl);

			log.debug("TEST: " + adminService.getVOName());

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
		}

	}
}
