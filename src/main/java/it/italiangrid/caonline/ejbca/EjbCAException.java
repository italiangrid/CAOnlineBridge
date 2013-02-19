package it.italiangrid.caonline.ejbca;

/**
 * Exception class for the personalized EjbCA method.
 * 
 * @author dmichelotto - diego.michelotto@cnaf.infn.it
 */
public class EjbCAException extends Exception {
	/** 
	 * Serial.
	 */
	private static final long serialVersionUID = -3844885146769278843L;

	/**
	 * Message of the exception.
	 */
	private String message;

	/** 
	 * Constructor.
	 * 
	 * @param message
	 *            - Exception message.
	 */
	public EjbCAException(final String message) {
		super();
		this.message = message;
	}

	/**
	 * Method that return the exception message.
	 * 
	 * @return The exception message.
	 */
	public final String getMessage() {
		return message;
	}
}
