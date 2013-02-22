package it.italiangrid.caonline.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.ejbca.core.protocol.ws.client.gen.Certificate;

/**
 * Class the display the information of the user certificate.
 * @author dmichelotto
 *
 */
public class CertificateInformation {
	
	/**
	 * Certificate subject.
	 */
	private String subject;
	
	/**
	 * Certificate issuer.
	 */
	private String issuer;
	
	/**
	 * Certificate profile.
	 */
	private String profile;
	
	/**
	 * Certificare creation date.
	 */
	private Date creationDate;
	
	/**
	 * Certificare expiration date.
	 */
	private Date expirationDate;
	
	/**
	 * 	The certificate.
	 */
	private String certificate;

	/**
	 * @param profile
	 * @throws CertificateException 
	 */
	public CertificateInformation(String profile, Certificate c) throws CertificateException {
		super();
		
		
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(c.getRawCertificateData());
		X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
		
		this.subject = cert.getSubjectDN().getName();
		this.issuer = cert.getIssuerDN().getName();
		this.profile = profile;
		this.creationDate = cert.getNotBefore();
		this.expirationDate = cert.getNotAfter();
		this.certificate = cert.toString().replaceAll("\n", "<br/>");
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}
	
	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDatee(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the certificate
	 */
	public String getCertificate() {
		return certificate;
	}

	/**
	 * @param certificate the certificate to set
	 */
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CertificateInformation [subject=" + subject + ", issuer="
				+ issuer + ", profile=" + profile + ", expirationDate="
				+ expirationDate + ", certificate=" + certificate + "]";
	}
}
