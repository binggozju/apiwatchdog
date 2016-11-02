package org.binggo.apiwatchdog.processor.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.config.Config;
import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * @author Binggo
 */
@Component("alarmProcessor")
public class AlarmProcessor extends WatchdogProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmProcessor.class);
	
	private Config config;
	
	private String senderUrl;
	private HttpClientUtils httpUtils;
	
	private JsonParser jsonParser;
	
	@Autowired
	public AlarmProcessor(WatchdogEnv env, Config config) {
		super(AlarmConstants.PROCESSOR_NAME);
		
		capacity = env.getInteger(AlarmConstants.QUEUE_CAPACITY_CONFIG, AlarmConstants.QUEUE_CAPACITY_DEFAULT);
		processorNum = env.getInteger(AlarmConstants.ALARM_THREAD_NUM_CONFIG, AlarmConstants.ALARM_THREAD_NUM_DEFAULT);
		senderUrl = env.getString(AlarmConstants.SENDER_URL_CONFIG, "");
		
		httpUtils = new HttpClientUtils();
		jsonParser = new JsonParser();
		this.config = config;
	}

	@Override
	protected void doInitialize() {
		// nothing to do
	}
	
	protected boolean isPermitted(Event event) {
		return config.shouldAlarm(event);
	}

	@Override
	public void close() {
		httpUtils.close();
		super.close();
	}
	
	/**
	 * support to send alarm messages
	 */
	@Override
	protected void processEvent(Event event) {
		sendWeixinMessage(event);
		sendMailMessage(event);
		sendSmsMessage(event);
	}
	
	private void sendWeixinMessage(Event event) {
		Integer alarmType = Integer.parseInt(event.getHeaders().get(AlarmTemplate.ALARM_TYPE_KEY));
		if ((alarmType & AlarmConstants.WEIXIN_ALARM_TYPE) != AlarmConstants.WEIXIN_ALARM_TYPE) {
			return;
		}
		String weixinSenderUrl = String.format("%s/weixin/async", senderUrl);
		String weixinReceivers = event.getHeaders().get(AlarmTemplate.WEIXIN_RECEIVERS_KEY);
		String weixinContent = AlarmTemplate.getAlarmMessage(event);
		
		JsonObject jsonParams = new JsonObject();
		jsonParams.addProperty("receivers", weixinReceivers);
		jsonParams.addProperty("content", weixinContent);
		jsonParams.addProperty("source", "apiwatchdog");
		
		try {
			String responseContent = httpUtils.sendPostRequest(weixinSenderUrl, jsonParams.toString());
			
			JsonObject jsonObj = jsonParser.parse(responseContent).getAsJsonObject();
			if (!jsonObj.has("retcode")) {
				logger.error(jsonObj.toString());
				return;
			}
			
			Integer retCode = jsonObj.get("retcode").getAsInt();
			if (retCode == 0) {
				logger.debug(String.format("send the weixin alarm message for api call [%s] successfully.", 
						((ApiCall) event.getBody()).toString()));
			} else {
				logger.error(String.format("fail to send the weixin alarm message for api call [%s]", 
						((ApiCall) event.getBody()).toString()));
			}
		} catch (WatchdogException ex) {
			logger.error(String.format("fail to send the weixin alarm message: %s", ex.getMessage()));
		}
	}
	
	private void sendMailMessage(Event event) {
		Integer alarmType = Integer.parseInt(event.getHeaders().get(AlarmTemplate.ALARM_TYPE_KEY));
		if ((alarmType & AlarmConstants.MAIL_ALARM_TYPE) != AlarmConstants.MAIL_ALARM_TYPE) {
			return;
		}
		String mailSenderUrl = String.format("%s/mail/async", senderUrl);
		String mailReceivers = event.getHeaders().get(AlarmTemplate.MAIL_RECEIVERS_KEY);
		String mailContent = AlarmTemplate.getAlarmMessage(event);
		
		JsonObject jsonParams = new JsonObject();
		jsonParams.addProperty("subject", "API调用监控实时告警");
		jsonParams.addProperty("receivers", mailReceivers);
		jsonParams.addProperty("content", mailContent);
		jsonParams.addProperty("source", "apiwatchdog");
		
		try {
			String responseContent = httpUtils.sendPostRequest(mailSenderUrl, jsonParams.toString());
			
			JsonObject jsonObj = jsonParser.parse(responseContent).getAsJsonObject();
			if (!jsonObj.has("retcode")) {
				logger.error(jsonObj.toString());
				return;
			}
			
			Integer retCode = jsonObj.get("retcode").getAsInt();
			if (retCode == 0) {
				logger.debug(String.format("send the mail alarm message for api call [%s] successfully.", 
						((ApiCall) event.getBody()).toString()));
			} else {
				logger.error(String.format("fail to send the mail alarm message for api call [%s]", 
						((ApiCall) event.getBody()).toString()));
			}	
			
		} catch (WatchdogException ex) {
			logger.error(String.format("fail to send the mail message: %s", ex.getMessage()));
		}
	}
	
	private void sendSmsMessage(Event event) {
		Integer alarmType = Integer.parseInt(event.getHeaders().get("alarmType"));
		if ((alarmType & AlarmConstants.SMS_ALARM_TYPE) != AlarmConstants.SMS_ALARM_TYPE) {
			return;
		}
		
		// Notice: do not support sending SMS message temporarily
		return;
	}		
	
	@Scheduled(initialDelay=1000, fixedDelay = 3000)
	@Override
	public void runTimerTask() {
		if (!isInitialized()) {
			logger.warn(String.format("%s has not been initialized, wait", getName()));
			return;
		}
		
		createAndCheckProcessors();
	}
	
}