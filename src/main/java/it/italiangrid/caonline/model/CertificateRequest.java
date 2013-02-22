package it.italiangrid.caonline.model;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.ScriptAssert;

/**
 * Class that define the request certificate model.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */

@ScriptAssert(lang = "javascript", script = "_this.proxyPass2.equals(_this.proxyPass1)", message = "The password must be the same")
public class CertificateRequest {

	/**
	 * User's e-mail.
	 */
	@NotEmpty
	private String mail;

	/**
	 * User's locality attribute.
	 */
	private String l;

	/**
	 * User's organization attribute.
	 */
	private String o;

	/**
	 * User's canonical name.
	 */
	@NotEmpty
	private String cn;

	/**
	 * User's password.
	 */
	@Size(min = 6, max = 10)
	private String proxyPass1;

	/**
	 * User's retyped password.
	 */
	private String proxyPass2;

	/**
	 * Condition term readed;
	 */
	@AssertTrue(message = "Please read and accept the condition term of use.")
	private boolean conditionTerm;
	
	/**
	 * User persisteId
	 */
	private String persistentId;
	
	/**
	 * Request come from portal or web
	 */
	private boolean fromPortal;

	/**
	 * Constructor.
	 * 
	 * @param mail
	 *            - User's e-mail
	 * @param cn
	 *            - User's Canonical Name
	 */
	public CertificateRequest(final String mail, final String cn) {
		this.mail = mail;
		this.cn = cn;
		this.l = "";
		this.o = "";
		this.proxyPass1 = "";
		this.proxyPass2 = "";
	}

	/**
	 * Default constructor.
	 */
	public CertificateRequest() {
		this.mail = "";
		this.cn = "";
		this.l = "";
		this.o = "";
		this.proxyPass1 = "";
		this.proxyPass2 = "";
	}

	/**
	 * @param mail
	 * @param l
	 * @param o
	 * @param cn
	 * @param proxyPass1
	 * @param proxyPass2
	 * @param conditionTerm
	 * @param persistentId
	 * @param fromPortal
	 */
	public CertificateRequest(String mail, String l, String o, String cn,
			String proxyPass1, String proxyPass2, boolean conditionTerm,
			String persistentId, boolean fromPortal) {
		super();
		this.mail = mail;
		this.l = l;
		this.o = o;
		this.cn = cn;
		this.proxyPass1 = proxyPass1;
		this.proxyPass2 = proxyPass2;
		this.conditionTerm = conditionTerm;
		this.persistentId = persistentId;
		this.fromPortal = fromPortal;
	}

	/**
	 * Getter e-mail method.
	 * 
	 * @return The User's e-mail
	 */
	public final String getMail() {
		return this.mail;
	}

	/**
	 * Setter e-mail method.
	 * 
	 * @param mail
	 *            - The User's e-mail.
	 */
	public final void setMail(final String mail) {
		this.mail = mail;
	}

	/**
	 * Getter Locality attribute method.
	 * 
	 * @return The user's Locality attribute.
	 */
	public final String getL() {
		return this.l;
	}

	/**
	 * Setter Locality attribute method.
	 * 
	 * @param l
	 *            - The user's Locality attribute.
	 */
	public final void setL(final String l) {
		this.l = l;
	}

	/**
	 * Getter Organization attribute method.
	 * 
	 * @return The user's Organization attribute.
	 */
	public final String getO() {
		return this.o;
	}

	/**
	 * Setter Organization attribute method.
	 * 
	 * @param o
	 *            - The user's Organization attribute.
	 */
	public final void setO(final String o) {
		this.o = o;
	}

	/**
	 * Getter Canonical Name method.
	 * 
	 * @return The user's Canonical Name.
	 */
	public final String getCn() {
		return this.cn;
	}

	/**
	 * Setter Canonical Name method.
	 * 
	 * @param cn
	 *            - The user's Canonical Name.
	 */
	public final void setCn(final String cn) {
		this.cn = cn;
	}

	/**
	 * Getter password method.
	 * 
	 * @return The user's password.
	 */
	public final String getProxyPass1() {
		return this.proxyPass1;
	}

	/**
	 * Setter password method.
	 * 
	 * @param proxyPass1
	 *            - The user's password.
	 */
	public final void setProxyPass1(final String proxyPass1) {
		this.proxyPass1 = proxyPass1;
	}

	/**
	 * Getter password method.
	 * 
	 * @return The user's retyped password.
	 */
	public final String getProxyPass2() {
		return this.proxyPass2;
	}

	/**
	 * Setter password method.
	 * 
	 * @param proxyPass2
	 *            - The user's retyped password.
	 */
	public final void setProxyPass2(final String proxyPass2) {
		this.proxyPass2 = proxyPass2;
	}

	/**
	 * @return the conditionTerm
	 */
	public boolean isConditionTerm() {
		return conditionTerm;
	}

	/**
	 * @param conditionTerm
	 *            the conditionTerm to set
	 */
	public void setConditionTerm(boolean conditionTerm) {
		this.conditionTerm = conditionTerm;
	}

	/**
	 * @return the persistentId
	 */
	public String getPersistentId() {
		return persistentId;
	}

	/**
	 * @param persistentId the persistentId to set
	 */
	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	/**
	 * @return the fromPortal
	 */
	public boolean isFromPortal() {
		return fromPortal;
	}

	/**
	 * @param fromPortal the fromPortal to set
	 */
	public void setFromPortal(boolean fromPortal) {
		this.fromPortal = fromPortal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CertificateRequest [mail=" + mail + ", l=" + l + ", o=" + o
				+ ", cn=" + cn + ", proxyPass1=" + proxyPass1 + ", proxyPass2="
				+ proxyPass2 + ", conditionTerm=" + conditionTerm
				+ ", persistentId=" + persistentId + ", fromPortal="
				+ fromPortal + "]";
	}

}
