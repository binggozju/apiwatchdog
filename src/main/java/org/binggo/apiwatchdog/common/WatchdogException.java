package org.binggo.apiwatchdog.common;

public class WatchdogException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param message the exception message
	 */
	public WatchdogException(String message) {
		super(message);
	}
	
	/**
	 * @param ex the causal exception
	 */
	public WatchdogException(Throwable ex) {
		super(ex);
	}
	
	/**
	 * @param message exception message
	 * @param ex the causal exception
	 */
	public WatchdogException(String message, Throwable ex) {
		super(message, ex);
	}

}
