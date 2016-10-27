package org.binggo.apiwatchdog.processor.alarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.domain.ApiCall;

public class AlarmTemplate {
	// used as keys to be added to the headers of the WatchdogEvent
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
	
	
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String getAlarmMessage(Event event) {
		ApiCall apiCall = (ApiCall) event.getBody();
		String apiCallUuid = Arrays.toString(apiCall.getCallUuid());
		
		switch (event.getHeaders().get(ALARM_REASON_KEY)) {
		case ALARM_REASON_NO_RESPONSE:
			return String.format("HTTP响应丢失告警：API调用(%s)未收到响应包，API ID为%d", 
					apiCallUuid, apiCall.getApiId());
			
		case ALARM_REASON_EXCEED_THRESHOLD:
			return String.format("响应时间超时告警：API调用(%s)收到响应包超时，API ID为%d，请求时间为%s，响应时间为%s", 
					apiCallUuid, apiCall.getApiId(), FORMAT.format(apiCall.getRequestTime()), 
					FORMAT.format(apiCall.getResponseTime()));
			
		case ALARM_REASON_NOT_HTTP200:
			return String.format("HTTP响应码非200告警：API调用(%s)的HTTP响应码为%s，API ID为%d", 
					apiCallUuid, apiCall.getHttpReponseCode(), apiCall.getApiId());
			
		case ALARM_REASON_NOT_RETCODE0:
			return String.format("返回码非0告警：API调用(%s)的返回码为%s，API ID为%d", 
					apiCallUuid, apiCall.getApiReturnCode(), apiCall.getApiId());
			
		default:
			return String.format("API调用错误告警：API调用(%s)存在错误，API ID为%d", 
					apiCallUuid, apiCall.getApiId());
		}
	}

}
