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
	
	INTERRUPTED (10, "operation has be interrupted"),
	
	FAIL_LOAD_PROCESSOR (80, "fail to load the processor"),
	FAIL_CREATE_PROCESSOR (81, "fail to create the processor"),
	FAILURE (99, "fail"),
	
	// return code for kafka agent [100, 200)
	FAIL_CONSTRUCT_CONSUMER (100, "fail to construct the kafka consumer"),
	
	// return code for collector module [200, 300)
	COLLECT_QUEUE_FULL (200, "collect queue is full"),
	INVALID_EVENT (201, "invalid event"),
	
	// return code for alarm processor module [300, 400)
	POST_HTTP_REQUEST_FAIL (300, "fail to send the post http request"),
	GET_HTTP_REQUEST_FAIL (301, "fail to send the get http request");
	
	
	// return code for processor module
	// return code for config module
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
