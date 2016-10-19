package org.binggo.apiwatchdog.common;

/**
 * The various of return code in apiwatchdog
 * @author Administrator
 *
 */
public enum ReturnCode {
	
	// common return code [0-100)
	OK (0, "ok"),
	FAILURE (1, "fail"),
	MISSING_FIELDS (2, "some fields are missing"),
	INVALID_PARAMETER (3, "some parameters are invalid");
	
	// return code for collector module
	// return code for processor module
	// return code for config module
	// return code for alarm module
	// return code for onlineanalyzer module
	// return code for statis module
	// return code for badcall module
	
	private final int code;
	private final String msg;
	
	private ReturnCode(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}
