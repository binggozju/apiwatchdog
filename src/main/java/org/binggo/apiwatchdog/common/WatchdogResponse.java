package org.binggo.apiwatchdog.common;

/**
 * WathcododResponse is an simple HTTP response without any data result.
 * @author Binggo
 */
public class WatchdogResponse {

	private Integer retCode;
	private String retMsg;
	
	public static final WatchdogResponse SIMPLE_OK_RESPONSE = new WatchdogResponse(ReturnCode.OK);
	public static final WatchdogResponse FAILURE_RESPONSE = new WatchdogResponse(ReturnCode.FAILURE);
	
	public WatchdogResponse(ReturnCode returnCode) {
		this(returnCode.getCode(), returnCode.getMsg());
	}
	
	public WatchdogResponse(Integer retCode, String retMsg) {
		this.retCode = retCode;
		this.retMsg = retMsg;
	}
	
	/**
	 * create a response with data result, and return it.
	 * @param returnCode
	 * @param result
	 * @return
	 */
	public static WatchdogResponse getResponse(ReturnCode returnCode, Object result) {
		return new ResponseWithData(returnCode, result);
	}
	
	/**
	 * create a response without data result, and return it.
	 * @param returnCode
	 * @return
	 */
	public static WatchdogResponse getResponse(ReturnCode returnCode) {
		return new WatchdogResponse(returnCode);
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
	
	/**
	 * ResponseWithData is an HTTP response with data.
	 * @author Binggo
	 */
	public static class ResponseWithData extends WatchdogResponse {
		private Object data;
		
		public ResponseWithData(ReturnCode returnCode, Object data) {
			super(returnCode);
			this.data = data;
		}
		
		public ResponseWithData(Integer retCode, String retMsg, Object data) {
			super(retCode, retMsg);
			this.data = data;
		}

		public Object getData() {
			return data;
		}

		public void setResult(Object data) {
			this.data = data;
		}
	}
	
}
