package it.italiangrid.caonline.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWS;
import org.ejbca.core.protocol.ws.client.gen.EjbcaWSService;

public class EjbcaWSConnection {
	
	private static final Logger log = Logger.getLogger(EjbcaWSConnection.class);
	
	private String ejbcaWsdlUrl;
	private String raCertPath;
	private String raCertPasswd;
	private String trustStorePath;
	private String trustStorePasswd;

	public EjbcaWSConnection() throws IOException {
		String contextPath = EjbcaWSConnection.class.getClassLoader().getResource("").getPath();
		
		log.debug("dove sono:" + contextPath);
		
		FileInputStream inStream =
	    new FileInputStream(contextPath + "/myejbca.properties");

		Properties prop = new Properties();
	
		prop.load(inStream);
	
		inStream.close();
		
		this.ejbcaWsdlUrl = prop.getProperty("wsdl.url");
		this.raCertPath = prop.getProperty("raCert.path");
		this.raCertPasswd = prop.getProperty("raCert.passwd");
		this.trustStorePath = prop.getProperty("trustStore.path");
		this.trustStorePasswd = prop.getProperty("trustStore.passwd");
		
		log.debug(ejbcaWsdlUrl);
	}
	
	public EjbcaWS getEjbcaWS() throws MalformedURLException{
		
		Security.addProvider (new BouncyCastleProvider());

		System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
		System.setProperty("javax.net.ssl.keyStore",this.raCertPath);
		System.setProperty("javax.net.ssl.keyStorePassword",this.raCertPasswd);
		  
		System.setProperty("javax.net.ssl.trustStore",this.trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword",this.trustStorePasswd);  
		  
		QName qname = new QName("http://ws.protocol.core.ejbca.org/", "EjbcaWSService");
		EjbcaWSService service = new EjbcaWSService(new URL(this.ejbcaWsdlUrl),qname);
		
		return service.getEjbcaWSPort();
	}

}
