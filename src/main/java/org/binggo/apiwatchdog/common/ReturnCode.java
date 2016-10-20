package org.binggo.apiwatchdog.common;

/**
 * The various of return code in apiwatchdog
 * @author Administrator
 *
 */
public enum ReturnCode {
	
	// common return code [0-100)
	OK (0, "ok"),
	INVALID_PARAMETER (1, "unvalid parameters"),
	
	FAIL_LOAD_PROCESSOR (80, "fail to load the processor"),
	FAIL_CREATE_PROCESSOR (81, "fail to create the processor"),
	FAILURE (99, "fail");
	
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
