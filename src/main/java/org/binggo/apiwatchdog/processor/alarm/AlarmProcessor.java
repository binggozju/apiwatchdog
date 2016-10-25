package org.binggo.apiwatchdog.processor.alarm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.binggo.apiwatchdog.WatchdogEvent;
import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.config.Config;
import org.binggo.apiwatchdog.domain.ApiCall;

@Component("alarmProcessor")
public class AlarmProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmProcessor.class);
	
	private Integer queueCapacity;
	private LinkedBlockingQueue<WatchdogEvent> alarmQueue;
	
	private Integer alarmRunnerNum;
	private static final String ALARM_THREAD_NAME = "Alarm-Runner-Thread";
	private Map<Thread, AlarmRunner> alarmRunnerMap;
	
	@Autowired
	private Config config;
	
	private String senderUrl;
	@Autowired
	private HttpClientUtils httpUtils;
	
	private JsonParser jsonParser;
	
	@Autowired
	public AlarmProcessor(WatchdogEnv env) {
		queueCapacity = env.getInteger(AlarmConstants.QUEUE_CAPACITY_CONFIG, AlarmConstants.QUEUE_CAPACITY_DEFAULT);
		alarmQueue = new LinkedBlockingQueue<WatchdogEvent>(queueCapacity);
		
		alarmRunnerNum = env.getInteger(AlarmConstants.ALARM_THREAD_NUM_CONFIG, AlarmConstants.ALARM_THREAD_NUM_DEFAULT);
		alarmRunnerMap = Maps.newHashMap();
		
		senderUrl = env.getString(AlarmConstants.SENDER_URL_CONFIG, "");
		jsonParser = new JsonParser();
	}

	@Override
	public void initialize() {
		logger.info("The AlarmProcessor has been initialized.");
	}

	/**
	 * put all the API call which needs to be alarmed to the alarmQueue
	 */
	@Override
	public void process(ApiCall apiCall) {
		WatchdogEvent alarmEvent = config.generateAlarmEvent(apiCall);
		if (alarmEvent == null) {
			return;
		}
		
		if (!alarmQueue.offer(alarmEvent)) {
			String callUuid = Arrays.toString(apiCall.getCallUuid());
			logger.error(String.format("fail to offer api call [%s] to alarm queue.", callUuid));
		}
	}

	@Override
	public void process(List<ApiCall> apiCallList) {
		for(ApiCall apiCall : apiCallList) {
			process(apiCall);
		}
	}

	@Override
	public void close() {
		httpUtils.close();
		logger.info("The BadCallProcessor has been closed.");
	}
	

	
	/**
	 * send alarm messages with multiple threads according to the API call information in alarmQueue
	 */
	@Scheduled(fixedDelay = 5*1000)
	public void alarm() {
		if (alarmRunnerMap.size() != 0) {
			logger.info("The alarm runner threads have already exist");
			return;
		}
		
		for (int i = 0; i < alarmRunnerNum; i++) {
			AlarmRunner alarmRunner = new AlarmRunner();
			
			Thread alarmRunnerThread = new Thread(alarmRunner);
			alarmRunnerThread.setName(String.format("%s-%d", ALARM_THREAD_NAME, i));
			
			alarmRunnerMap.put(alarmRunnerThread, alarmRunner);
			
			alarmRunnerThread.start();
		}	
	}
	
	public void stopAllAlarmRunners() {
		for (Map.Entry<Thread, AlarmRunner> entry : alarmRunnerMap.entrySet()) {
			AlarmRunner alarmRunner = entry.getValue();
			Thread alarmRunnerThread = entry.getKey();
			
			alarmRunner.setShouldStop(true);
			alarmRunnerThread.interrupt();
			
			while(alarmRunnerThread.isAlive()) {
				try {
					alarmRunnerThread.join(500);
				} catch (InterruptedException ex) {
					logger.info("Interrupted while waiting for runner thread to exit. Exception follows.", ex);
				}
			}
		}
	}
	
	/**
	 * support to send alarm messages with multiple threads
	 */
	private class AlarmRunner implements Runnable {
		
		private AtomicBoolean shouldStop = new AtomicBoolean(false);

		@Override
		public void run() {
			while (!shouldStop.get()) {
				WatchdogEvent event = alarmQueue.poll();
				if (event == null) {
					try {
						Thread.sleep(AlarmConstants.IDLE_SLEEP_TIME*1000);
						continue;
					} catch (InterruptedException ex) {
						logger.warn(ex.getMessage());
						continue;
					}
				}
				sendWeixinMessage(event);
				sendMailMessage(event);
				sendSmsMessage(event);
			}
		}
		
		private void sendWeixinMessage(WatchdogEvent event) {
			Integer alarmType = Integer.parseInt(event.getHeaders().get(AlarmTemplate.ALARM_TYPE_KEY));
			if ((alarmType & AlarmConstants.WEIXIN_ALARM_TYPE) != AlarmConstants.WEIXIN_ALARM_TYPE) {
				return;
			}
			String weixinSenderUrl = String.format("%s/weixin/async", senderUrl);
			String weixinReceivers = event.getHeaders().get(AlarmTemplate.WEIXIN_RECEIVERS_KEY);
			String weixinContent = AlarmTemplate.getAlarmMessage(event);
			
			Map<String, String> postContentMap = Maps.newHashMap();
			postContentMap.put("receivers", weixinReceivers);
			postContentMap.put("content", weixinContent);
			postContentMap.put("source", "apiwatchdog");
			
			try {
				String responseContent = httpUtils.sendPostRequest(weixinSenderUrl, postContentMap);
				
				JsonObject jsonObj = jsonParser.parse(responseContent).getAsJsonObject();
				Integer retCode = jsonObj.get("retcode").getAsInt();
				if (retCode != 0) {
					//logger.error("");
				}	
			} catch (WatchdogException ex) {
				logger.error(String.format("fail to send the weixin message: %s", ex.getMessage()));
			}
		}
		
		private void sendMailMessage(WatchdogEvent event) {
			Integer alarmType = Integer.parseInt(event.getHeaders().get(AlarmTemplate.ALARM_TYPE_KEY));
			if ((alarmType & AlarmConstants.MAIL_ALARM_TYPE) != AlarmConstants.MAIL_ALARM_TYPE) {
				return;
			}
			String mailSenderUrl = String.format("%s/mail/async", senderUrl);
			String mailReceivers = event.getHeaders().get(AlarmTemplate.MAIL_RECEIVERS_KEY);
			String mailContent = AlarmTemplate.getAlarmMessage(event);
			
			Map<String, String> postContentMap = Maps.newHashMap();
			postContentMap.put("subject", "API调用监控实时告警");
			postContentMap.put("receivers", mailReceivers);
			postContentMap.put("content", mailContent);
			postContentMap.put("source", "apiwatchdog");
			
			try {
				String responseContent = httpUtils.sendPostRequest(mailSenderUrl, postContentMap);
				
				JsonObject jsonObj = jsonParser.parse(responseContent).getAsJsonObject();
				Integer retCode = jsonObj.get("retcode").getAsInt();
				if (retCode != 0) {
					//logger.error("");
				}	
				
			} catch (WatchdogException ex) {
				logger.error(String.format("fail to send the mail message: %s", ex.getMessage()));
			}
		}
		
		private void sendSmsMessage(WatchdogEvent event) {
			Integer alarmType = Integer.parseInt(event.getHeaders().get("alarmType"));
			if ((alarmType & AlarmConstants.SMS_ALARM_TYPE) != AlarmConstants.SMS_ALARM_TYPE) {
				return;
			}
			
			// Notice: do not support sending SMS message temporarily
			return;
		}
		
		public void setShouldStop(Boolean stop) {
			shouldStop.set(stop);
		}
		
	}
	
}
