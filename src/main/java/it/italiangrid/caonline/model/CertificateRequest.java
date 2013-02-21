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
	 * Constructor.
	 * 
	 * @param mail
	 *            - User's e-mail
	 * @param cn
	 *            - User's Canonical Name
	 * @param l
	 *            - User's Locality attribute
	 * @param o
	 *            - User's Organization attribute
	 * @param proxyPass1
	 *            - User's password
	 * @param proxyPass2
	 *            - User's retyped password
	 */
	public CertificateRequest(final String mail, final String cn,
			final String l, final String o, final String proxyPass1,
			final String proxyPass2) {
		this.mail = mail;
		this.cn = cn;
		this.l = l;
		this.o = o;
		this.proxyPass1 = proxyPass1;
		this.proxyPass2 = proxyPass2;
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
	 * toString overwrited method.
	 * 
	 * @return The certificate request.
	 */
	@Override
	public final String toString() {
		return "CN =       " + this.cn + "\n" + "O =        " + this.o + "\n"
				+ "L =        " + this.l + "\n" + "Mail =     " + this.mail
				+ "\n" + "ProxyPass1 = " + this.proxyPass1 + "\n"
				+ "ProxyPass2 = " + this.proxyPass2 + "\n"
				+ "Condition Term of Use = " + this.conditionTerm + "\n";
	}

}
