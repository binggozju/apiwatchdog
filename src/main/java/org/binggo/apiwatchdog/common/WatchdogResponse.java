package org.binggo.apiwatchdog.common;

public class WatchdogResponse {
	
	private Integer retCode;
	private String retMsg;
	
	private Object result;
	
	// return ok without result
	public static final WatchdogResponse OK_RESPONSE = new WatchdogResponse(
			ReturnCode.OK, CollectionUtils.EMPTY_LIST);
	
	// return failure without result
	public static final WatchdogResponse FAILURE_RESPONSE = new WatchdogResponse(
			ReturnCode.FAILURE, CollectionUtils.EMPTY_LIST);
	
	public WatchdogResponse(ReturnCode returnCode) {
		this(returnCode.getCode(), returnCode.getMsg(), CollectionUtils.EMPTY_LIST);
	}
	
	public WatchdogResponse(ReturnCode returnCode, Object result) {
		this(returnCode.getCode(), returnCode.getMsg(), result);
	}
	
	public WatchdogResponse(Integer retCode, String retMsg, Object result) {
		this.retCode = retCode;
		this.retMsg = retMsg;
		this.result = result;
	}

	public Integer getRetCode() {
		return retCode;
	}

	public void setRetCode(Integer retCode) {
		this.retCode = retCode;
	}

	public String getRetMsg() {
		return retMsg;
	}

	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
