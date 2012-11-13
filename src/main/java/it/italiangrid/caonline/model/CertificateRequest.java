package it.italiangrid.caonline.model;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.ScriptAssert;

/**
 * Class that define the request certificate model
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */

@ScriptAssert(lang = "javascript", script = "_this.proxyPass2.equals(_this.proxyPass1)", message = "The password must be the same")
public class CertificateRequest {

	/**
	 * User's e-mail
	 */
	@NotEmpty
	private String mail;

	/**
	 * User's locality attribute
	 */
	private String l;

	/**
	 * User's organization attribute
	 */
	private String o;

	/**
	 * User's canonical name
	 */
	@NotEmpty
	private String cn;

	/**
	 * User's password
	 */
	@Size(min = 6, max = 10)
	private String proxyPass1;
	private String proxyPass2;

	/**
	 * Constructor
	 * 
	 * @param mail
	 *            - User's e-mail
	 * @param cn
	 *            - User's Canonical Name
	 */
	public CertificateRequest(String mail, String cn) {
		this.mail = mail;
		this.cn = cn;
		this.l = "";
		this.o = "";
		this.proxyPass1 = "";
		this.proxyPass2 = "";
	}

	/**
	 * Default constructor
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
	 * Constructor
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
	public CertificateRequest(String mail, String cn, String l, String o,
			String proxyPass1, String proxyPass2) {
		this.mail = mail;
		this.cn = cn;
		this.l = l;
		this.o = o;
		this.proxyPass1 = proxyPass1;
		this.proxyPass2 = proxyPass2;
	}

	/**
	 * Getter e-mail method
	 * 
	 * @return The User's e-mail
	 */
	public String getMail() {
		return this.mail;
	}

	/**
	 * Setter e-mail method
	 * 
	 * @param mail
	 *            - The User's e-mail
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * Getter Locality attribute method
	 * 
	 * @return The user's Locality attribute
	 */
	public String getL() {
		return this.l;
	}

	/**
	 * Setter Locality attribute method
	 * 
	 * @param o
	 *            - The user's Locality attribute
	 */
	public void setL(String l) {
		this.l = l;
	}

	/**
	 * Getter Organization attribute method
	 * 
	 * @return The user's Organization attribute
	 */
	public String getO() {
		return this.o;
	}

	/**
	 * Setter Organization attribute method
	 * 
	 * @param o
	 *            - The user's Organization attribute
	 */
	public void setO(String o) {
		this.o = o;
	}

	/**
	 * Getter Canonical Name method
	 * 
	 * @return The user's Canonical Name
	 */
	public String getCn() {
		return this.cn;
	}

	/**
	 * Setter Canonical Name method
	 * 
	 * @param cn
	 *            - The user's Canonical Name
	 */
	public void setCn(String cn) {
		this.cn = cn;
	}

	/**
	 * Getter password method
	 * 
	 * @return The user's password
	 */
	public String getProxyPass1() {
		return this.proxyPass1;
	}

	/**
	 * Setter password method
	 * 
	 * @param proxyPass2
	 *            - The user's password
	 */
	public void setProxyPass1(String proxyPass1) {
		this.proxyPass1 = proxyPass1;
	}

	/**
	 * Getter password method
	 * 
	 * @return The user's retyped password
	 */
	public String getProxyPass2() {
		return this.proxyPass2;
	}

	/**
	 * Setter password method
	 * 
	 * @param proxyPass2
	 *            - The user's retyped password
	 */
	public void setProxyPass2(String proxyPass2) {
		this.proxyPass2 = proxyPass2;
	}

	/**
	 * toString overwrited method
	 */
	public String toString() {
		return "CN =       " + this.cn + "\n" + "O =        " + this.o + "\n"
				+ "L =        " + this.l + "\n" + "Mail =     " + this.mail
				+ "\n" + "ProxyPass1 " + this.proxyPass1 + "\n" + "ProxyPass2 "
				+ this.proxyPass2 + "\n";
	}

}
