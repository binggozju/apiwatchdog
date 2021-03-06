package org.binggo.apiwatchdog.processor.alarm;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.common.CommonUtils;
import org.binggo.apiwatchdog.domain.ApiCall;

public class AlarmTemplate {
	// used as keys to be added to the headers of the Event
	public static final String ALARM_REASON_KEY = "alarmReason";
	public static final String ALARM_TYPE_KEY = "alarmType";
	public static final String WEIXIN_RECEIVERS_KEY = "weixinReceivers";
	public static final String MAIL_RECEIVERS_KEY = "mailReceivers";
	public static final String SMS_RECEIVERS_KEY = "phoneReceivers";
	
	// common reasons of alarming
	public static final String ALARM_REASON_NO_RESPONSE = "no_response";
	public static final String ALARM_REASON_EXCEED_THRESHOLD = "exceed_threshold";
	public static final String ALARM_REASON_NOT_HTTP200 = "not_http_200";
	public static final String ALARM_REASON_NOT_RETCODE0 = "not_retcode_0";
	
	public static String getAlarmMessage(Event event) {
		ApiCall apiCall = (ApiCall) event.getBody();
		
		Integer apiId = apiCall.getApiId();
		String requestTime = CommonUtils.getNormalDateFormat().format(apiCall.getRequestTime());
		String source = apiCall.getSourceService();
		//String apiCallUuid = apiCall.getCallUuid();
		
		switch (event.getHeaders().get(ALARM_REASON_KEY)) {
		case ALARM_REASON_NO_RESPONSE:
			return String.format("[HTTP响应丢失告警] API（id:%d）于%s收到%s发起的调用响应丢失", 
					apiId, requestTime, source);
			/*return String.format("[HTTP响应丢失告警] API（id:%d）于%s收到%s发起的调用（调用id:%s）未收到响应包", 
					apiId, requestTime, source, apiCallUuid); */
			
		case ALARM_REASON_EXCEED_THRESHOLD:
			int timeDelta = (int)(apiCall.getResponseTime().getTime() - apiCall.getRequestTime().getTime())/1000;
			return String.format("[响应超时告警] API（id:%d）于%s收到%s发起的调用响应时间%d秒", 
					apiId, requestTime, source, timeDelta);
			/*return String.format("[响应超时告警] API（id:%d）于%s收到%s发起的调用（调用id:%s）响应时间%d秒", 
					apiId, requestTime, source, apiCallUuid, timeDelta); */
			
		case ALARM_REASON_NOT_HTTP200:
			return String.format("[HTTP响应码非200告警] API（id:%d）于%s收到%s发起的调用HTTP响应码为%s",
					apiId, requestTime, source, apiCall.getHttpResponseCode());
			/*return String.format("[HTTP响应码非200告警] API（id:%d）于%s收到%s发起的调用（调用id:%s）HTTP响应码为%s",
					apiId, requestTime, source, apiCallUuid, apiCall.getHttpResponseCode()); */
			
		case ALARM_REASON_NOT_RETCODE0:
			return String.format("[返回码非0告警] API（id:%d）于%s收到%s发起的调用返回码为%s",
					apiId, requestTime, source, apiCall.getApiReturnCode());
			/*return String.format("[返回码非0告警] API（id:%d）于%s收到%s发起的调用（调用id:%s）返回码为%s",
					apiId, requestTime, source, apiCallUuid, apiCall.getApiReturnCode()); */
			
		default:
			return String.format("[API调用错误告警] API（id:%d）于%s收到%s发起的调用发生错误", 
					apiId, requestTime, source);
			/*return String.format("[API调用错误告警] API（id:%d）于%s收到%s发起的调用（调用id:%s）发生错误", 
					apiId, requestTime, source, apiCallUuid); */
		}
	}

}
