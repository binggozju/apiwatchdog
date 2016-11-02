package org.binggo.apiwatchdog.processor.alarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.binggo.apiwatchdog.Event;
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
	
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static String getAlarmMessage(Event event) {
		ApiCall apiCall = (ApiCall) event.getBody();
		
		Integer apiId = apiCall.getApiId();
		String requestTime = DATE_FORMAT.format(apiCall.getRequestTime());
		String source = apiCall.getSourceService();
		String apiCallUuid = apiCall.getCallUuid();
		
		switch (event.getHeaders().get(ALARM_REASON_KEY)) {
		case ALARM_REASON_NO_RESPONSE:
			return String.format("[HTTP响应丢失告警] API（id为%d）在%s时间收到来自%s的一次调用（调用id为%s）未收到响应包", 
					apiId, requestTime, source, apiCallUuid);
			
		case ALARM_REASON_EXCEED_THRESHOLD:
			int timeDelta = (int)(apiCall.getResponseTime().getTime() - apiCall.getRequestTime().getTime())/1000;
			return String.format("[响应时间超时告警] API（id为%d）在%s时间收到来自%s的一次调用（调用id为%s）响应时间超时，响应时间为%d秒", 
					apiId, requestTime, source, apiCallUuid, timeDelta);
			
		case ALARM_REASON_NOT_HTTP200:
			return String.format("[HTTP响应码非200告警] API（id为%d）在%s时间收到来自%s的一次调用（调用id为%s）的HTTP响应码为%s",
					apiId, requestTime, source, apiCallUuid, apiCall.getHttpReponseCode());
			
		case ALARM_REASON_NOT_RETCODE0:
			return String.format("[返回码非0告警] API（id为%d）在%s时间收到来自%s的一次调用（调用id为%s）的调用返回码为%s",
					apiId, requestTime, source, apiCallUuid, apiCall.getApiReturnCode());
			
		default:
			return String.format("[API调用错误告警] API（id为%d）在%s时间收到来自%s的一次调用（调用id为%s）发生错误", 
					apiId, requestTime, source, apiCallUuid);
		}
	}

}
