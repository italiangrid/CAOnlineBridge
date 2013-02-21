package it.italiangrid.caonline.model;

import java.math.BigInteger;

import javax.validation.constraints.AssertTrue;

public class RevokeRequest {
	
	/**
	 * The certificate issuer DN
	 */
	private String subjectDN;

	/**
	 * The certificate issuer DN
	 */
	private String issuerDN;
	
	/**
	 * The certificate Serial Number
	 */
	private String certificateSN;
	
	/**
	 * The revocation reason
	 */
	private int reason;
	
	/**
	 * Is request from Portal
	 */
	private boolean portalRequest;
	
	/**
	 * Is accepted?
	 */
	@AssertTrue(message = "Please accept.")
	private boolean accepted;
	
	/**
	 * 
	 */
	public RevokeRequest() {
		super();
	}
	
	/**
	 * @param subjectDN
	 * @param issuerDN
	 * @param certificateSN
	 * @param reason
	 * @param portalRequest
	 * @param accepted
	 */
	public RevokeRequest(String subjectDN, String issuerDN,
			String certificateSN, int reason, boolean portalRequest,
			boolean accepted) {
		super();
		this.subjectDN = subjectDN;
		this.issuerDN = issuerDN;
		this.certificateSN = certificateSN;
		this.reason = reason;
		this.portalRequest = portalRequest;
		this.accepted = accepted;
	}



	/**
	 * @return the subjectDN
	 */
	public String getSubjectDN() {
		return subjectDN;
	}

	/**
	 * @param subjectDN the subjectDN to set
	 */
	public void setSubjectDN(String subjectDN) {
		this.subjectDN = subjectDN;
	}

	/**
	 * @return the issuerDN
	 */
	public String getIssuerDN() {
		return issuerDN;
	}

	/**
	 * @param issuerDN the issuerDN to set
	 */
	public void setIssuerDN(String issuerDN) {
		this.issuerDN = issuerDN;
	}

	/**
	 * @return the certificateSN
	 */
	public String getCertificateSN() {
		return certificateSN;
	}

	/**
	 * @param certificateSN the certificateSN to set
	 */
	public void setCertificateSN(String certificateSN) {
		this.certificateSN = certificateSN;
	}

	/**
	 * @return the reason
	 */
	public int getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(int reason) {
		this.reason = reason;
	}

	/**
	 * @return the portalRequest
	 */
	public boolean isPortalRequest() {
		return portalRequest;
	}

	/**
	 * @param portalRequest the portalRequest to set
	 */
	public void setPortalRequest(boolean portalRequest) {
		this.portalRequest = portalRequest;
	}

	/**
	 * @return the accepted
	 */
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * @param accepted the accepted to set
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RevokeRequest [subjectDN=" + subjectDN + ", issuerDN="
				+ issuerDN + ", certificateSN=" + certificateSN + ", reason="
				+ reason + ", portalRequest=" + portalRequest + ", accepted="
				+ accepted + "]";
	}
	
	
}
