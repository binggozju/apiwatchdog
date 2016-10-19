package org.binggo.apiwatchdog.common;

public class WatchdogException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	private ReturnCode returnCode;
	
	/*public WatchdogException(String message) {
		super(message);
	}
	
	public WatchdogException(Throwable ex) {
		super(ex);
	}
	
	public WatchdogException(String message, Throwable ex) {
		super(message, ex);
	}*/
	
	public WatchdogException(ReturnCode returnCode) {
		this(returnCode, returnCode.getMsg());
	}
	
	public WatchdogException(ReturnCode returnCode, String message) {
		super(message);
		this.returnCode = returnCode;
	}

	public ReturnCode getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(ReturnCode returnCode) {
		this.returnCode = returnCode;
	}

}
