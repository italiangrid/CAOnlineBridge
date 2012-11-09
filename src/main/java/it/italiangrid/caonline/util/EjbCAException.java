package it.italiangrid.caonline.util;

public class EjbCAException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3844885146769278843L;
	
	private String message;
	
	public EjbCAException(String message){
		super();
		this.message=message;
	}
	
	public String getMessage(){
		return message;
	}
}
