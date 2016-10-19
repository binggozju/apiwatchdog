package org.binggo.apiwatchdog.common;

public class WatchdogResponse {
	
	private Integer retCode;
	private String retMsg;
	
	private Object result;
	
	public WatchdogResponse(Object result) {
		this(ReturnCode.OK, result);
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
