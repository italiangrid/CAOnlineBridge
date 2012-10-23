package it.italiangrid.caonline.model;

import java.security.cert.X509Certificate;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.ScriptAssert;

@ScriptAssert(lang = "javascript", script = "_this.proxyPass2.equals(_this.proxyPass1)", message = "The password must be the same")
public class CertificateRequest {
	
	@NotEmpty
	private String mail;
	private String l;
	private String o;
	@NotEmpty
	private String cn;
	private X509Certificate cert;
	
	public X509Certificate getCert() {
		return cert;
	}

	public void setCert(X509Certificate cert) {
		this.cert = cert;
	}

	@Size(min=6, max=10)
	private String proxyPass1;
	private String proxyPass2;
	
	public CertificateRequest(String mail, String cn) {
		this.mail = mail;
		this.cn = cn;
		this.l="";
		this.o="";
		this.proxyPass1="";
		this.proxyPass2="";
	}
	
	public CertificateRequest() {
		this.mail = "";
		this.cn = "";
		this.l="";
		this.o="";
		this.proxyPass1="";
		this.proxyPass2="";
	}
	
	public CertificateRequest(String mail, String cn, String l, String o, String proxyPass1, String proxyPass2) {
		this.mail = mail;
		this.cn = cn;
		this.l=l;
		this.o=o;
		this.proxyPass1=proxyPass1;
		this.proxyPass2=proxyPass2;
	}
	
	public String getMail() {
		return this.mail;
	}
	
	public void setMail(String mail) {
		this.mail = mail;
	}
	
	public String getL() {
		return this.l;
	}
	
	public void setL(String l) {
		this.l = l;
	}
	public String getO() {
		return this.o;
	}
	
	public void setO(String o) {
		this.o = o;
	}
	public String getCn() {
		return this.cn;
	}
	
	public void setCn(String cn) {
		this.cn = cn;
	}
	public String getProxyPass1() {
		return this.proxyPass1;
	}
	
	public void setProxyPass1(String proxyPass1) {
		this.proxyPass1 = proxyPass1;
	}
	public String getProxyPass2() {
		return this.proxyPass2;
	}
	
	public void setProxyPass2(String proxyPass2) {
		this.proxyPass2 = proxyPass2;
	}
	
	public String toString(){
		return	"CN =       " + this.cn + "\n" +
				"O =        " + this.o + "\n" +
				"L =        " + this.l + "\n" +
				"Mail =     " + this.mail + "\n" +
				"ProxyPass1 " + this.proxyPass1 + "\n" + 
				"ProxyPass2 " + this.proxyPass2 + "\n";
	}

}
