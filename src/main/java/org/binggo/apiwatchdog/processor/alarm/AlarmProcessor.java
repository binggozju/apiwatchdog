package org.binggo.apiwatchdog.processor.alarm;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.config.ConfigProvider;
import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * @author Binggo
 */
@Component("alarmProcessor")
public class AlarmProcessor extends WatchdogProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmProcessor.class);
	
	private Integer weixinAlarmQuota;
	private Integer mailAlarmQuota;
	@SuppressWarnings("unused")
	private Integer smsAlarmQuota;
	// alarm type -> (api id -> the number of alarm messages)
	private Map<String, Map<Integer, AtomicInteger> > alarmCounter;
	private ThreadPoolTaskScheduler taskScheduler;  // used to reset the alarm counter every two hours
	
	private ConfigProvider configProvider;
	
	private String senderUrl;
	private HttpClientUtils httpUtils;
	
	private JsonParser jsonParser;
	
	@Autowired
	public AlarmProcessor(WatchdogEnv env, ConfigProvider configProvider) {
		super(AlarmConstants.PROCESSOR_NAME);
		
		weixinAlarmQuota = env.getInteger(AlarmConstants.ALARM_WEIXIN_QUOTA_CONFIG, AlarmConstants.ALARM_WEIXIN_QUOTA_DEFAULT);
		mailAlarmQuota = env.getInteger(AlarmConstants.ALARM_MAIL_QUOTA_CONFIG, AlarmConstants.ALARM_MAIL_QUOTA_DEFAULT);
		smsAlarmQuota = env.getInteger(AlarmConstants.ALARM_SMS_QUOTA_CONFIG, AlarmConstants.ALARM_SMS_QUOTA_DEFAULT);
		alarmCounter = Maps.newHashMap();
		alarmCounter.put(AlarmConstants.WEIXIN_ALARM_COUNTER_NAME, new ConcurrentHashMap<Integer, AtomicInteger>());
		alarmCounter.put(AlarmConstants.MAIL_ALARM_COUNTER_NAME, new ConcurrentHashMap<Integer, AtomicInteger>());
		alarmCounter.put(AlarmConstants.SMS_ALARM_COUNTER_NAME, new ConcurrentHashMap<Integer, AtomicInteger>());
		
		taskScheduler = new ThreadPoolTaskScheduler();
		
		capacity = env.getInteger(AlarmConstants.QUEUE_CAPACITY_CONFIG, AlarmConstants.QUEUE_CAPACITY_DEFAULT);
		processorNum = env.getInteger(AlarmConstants.ALARM_THREAD_NUM_CONFIG, AlarmConstants.ALARM_THREAD_NUM_DEFAULT);
		senderUrl = env.getString(AlarmConstants.SENDER_URL_CONFIG, "");
		
		httpUtils = new HttpClientUtils();
		jsonParser = new JsonParser();
		this.configProvider = configProvider;
	}

	@Override
	protected void doInitialize() {
		// initial the ThreadPoolTaskScheduler
		taskScheduler.setPoolSize(AlarmConstants.SCHEDULE_POOL_SIZE_DEFAULT);
		taskScheduler.initialize();
		
		Date now = new Date();
		long startResetTime = (now.getTime()/(AlarmConstants.RESET_COUNTER_SECONDS_DEFAULT*1000) + 1) * 
				(AlarmConstants.RESET_COUNTER_SECONDS_DEFAULT*1000);
		Date startTime = new Date(startResetTime);
		taskScheduler.scheduleAtFixedRate(
				new Runnable() {
					@Override
					public void run() {
						resetCounter();
					}
				}, 
				startTime, 
				AlarmConstants.RESET_COUNTER_SECONDS_DEFAULT*1000);
	}
	
	protected boolean isPermitted(Event event) {
		return configProvider.shouldAlarm(event);
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
		Integer apiId = ((ApiCall) event.getBody()).getApiId();
		String weixinSenderUrl = String.format("%s/weixin/async", senderUrl);
		String weixinReceivers = event.getHeaders().get(AlarmTemplate.WEIXIN_RECEIVERS_KEY);
		String weixinContent = AlarmTemplate.getAlarmMessage(event);
		
		if (weixinReceivers == null || weixinReceivers.equals("")) {
			logger.error(String.format("The weixin receivers of API [%d] is null, quit", apiId));
			return;
		}
		
		JsonObject jsonParams = new JsonObject();
		jsonParams.addProperty("receivers", weixinReceivers);
		jsonParams.addProperty("content", weixinContent);
		jsonParams.addProperty("source", "apiwatchdog");
		
		try {
			// check the quota of sending weixin messages
			alarmCounter.get(AlarmConstants.WEIXIN_ALARM_COUNTER_NAME).putIfAbsent(apiId, new AtomicInteger(0));
			int currentNum = alarmCounter.get(AlarmConstants.WEIXIN_ALARM_COUNTER_NAME).get(apiId).getAndIncrement();

			if (currentNum >= weixinAlarmQuota) {
				logger.debug(String.format("quota of sending weixin msg for api[%d] has been exceeded", apiId));
				alarmCounter.get(AlarmConstants.WEIXIN_ALARM_COUNTER_NAME).get(apiId).decrementAndGet();
				return;
			}
			String responseContent = httpUtils.sendPostRequest(weixinSenderUrl, jsonParams.toString());
			
			JsonObject jsonObj = jsonParser.parse(responseContent).getAsJsonObject();
			if (!jsonObj.has("retcode")) {
				alarmCounter.get(AlarmConstants.WEIXIN_ALARM_COUNTER_NAME).get(apiId).decrementAndGet();
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
			alarmCounter.get(AlarmConstants.WEIXIN_ALARM_COUNTER_NAME).get(apiId).decrementAndGet();
			logger.error(String.format("fail to send the weixin alarm message: %s", ex.getMessage()));
		}
	}
	
	private void sendMailMessage(Event event) {
		Integer alarmType = Integer.parseInt(event.getHeaders().get(AlarmTemplate.ALARM_TYPE_KEY));
		if ((alarmType & AlarmConstants.MAIL_ALARM_TYPE) != AlarmConstants.MAIL_ALARM_TYPE) {
			return;
		}
		Integer apiId = ((ApiCall) event.getBody()).getApiId();
		String mailSenderUrl = String.format("%s/mail/async", senderUrl);
		String mailReceivers = event.getHeaders().get(AlarmTemplate.MAIL_RECEIVERS_KEY);
		String mailContent = AlarmTemplate.getAlarmMessage(event);
		
		if (mailReceivers == null || mailReceivers.equals("")) {
			logger.error(String.format("The mail receivers of API [%d] is null, quit", apiId));
			return;
		}
		
		JsonObject jsonParams = new JsonObject();
		jsonParams.addProperty("subject", "API监控实时告警");
		jsonParams.addProperty("receivers", mailReceivers);
		jsonParams.addProperty("content", mailContent);
		jsonParams.addProperty("source", "apiwatchdog");
		
		try {
			// check the quota of sending mail messages
			alarmCounter.get(AlarmConstants.MAIL_ALARM_COUNTER_NAME).putIfAbsent(apiId, new AtomicInteger(0));
			int currentNum = alarmCounter.get(AlarmConstants.MAIL_ALARM_COUNTER_NAME).get(apiId).getAndIncrement();
	
			if (currentNum >= mailAlarmQuota) {
				logger.debug(String.format("quota of sending mail msg for api[%d] has been exceeded", apiId));
				alarmCounter.get(AlarmConstants.MAIL_ALARM_COUNTER_NAME).get(apiId).decrementAndGet();
				return;
			}
			String responseContent = httpUtils.sendPostRequest(mailSenderUrl, jsonParams.toString());
			
			JsonObject jsonObj = jsonParser.parse(responseContent).getAsJsonObject();
			if (!jsonObj.has("retcode")) {
				alarmCounter.get(AlarmConstants.MAIL_ALARM_COUNTER_NAME).get(apiId).decrementAndGet();
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
			alarmCounter.get(AlarmConstants.MAIL_ALARM_COUNTER_NAME).get(apiId).decrementAndGet();
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
	
	/**
	 * reset the alarmCounter every two hours
	 */
	private void resetCounter() {
		for (Map.Entry<String, Map<Integer, AtomicInteger>> entry : alarmCounter.entrySet()) {
			for (Map.Entry<Integer, AtomicInteger> counterEntry : entry.getValue().entrySet()) {
				AtomicInteger counter = counterEntry.getValue();
				counter.set(0);
			}
		}
		logger.info("reset the alarm counter successfully");
	}
	
	@Scheduled(initialDelay=1000, fixedDelay=3*1000)
	@Override
	public void runTimerTask() {
		if (!isInitialized()) {
			logger.warn(String.format("%s has not been initialized, wait", getName()));
			return;
		}
		
		createAndCheckProcessors();
	}
	
}