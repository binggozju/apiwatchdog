package org.binggo.apiwatchdog.processor.alarm;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.config.Config;

@Component("alarmProcessor")
public class AlarmProcessor extends WatchdogProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmProcessor.class);
	
	private Integer capacity;
	private Integer processorNum;
	
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
		
		processQueue = new LinkedBlockingQueue<Event>(capacity);
		httpUtils = new HttpClientUtils();
		jsonParser = new JsonParser();
		
		this.config = config;
	}

	@Override
	public void initialize() {
		super.initialize();
	}
	
	protected Boolean isPermitted(Event event) {
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
	public void process() {
		Event event = processQueue.poll();
		if (event == null) {
			try {
				Thread.sleep(AlarmConstants.IDLE_SLEEP_TIME*1000);
			} catch (InterruptedException ex) {
				logger.warn(ex.getMessage());
			}
			return;
		}
		
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
	
	private void sendMailMessage(Event event) {
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
		
		if (runnerMap.size() == 0) {
			ProcessorRunner alarmRunner = new ProcessorRunner(this);
			for (int i = 0; i < processorNum; i++) {
				Thread alarmThread = new Thread(alarmRunner);
				
				alarmThread.setName(String.format("%s-%d", AlarmConstants.PROCESSOR_NAME, i));
				
				runnerMap.put(alarmThread, alarmRunner);
				logger.info(String.format("start the alarm processor thread [%s]", alarmThread.getName()));
				alarmThread.start();
			}
			
			return;
		}
		
		for (Map.Entry<Thread, ProcessorRunner> entry : runnerMap.entrySet()) {
			ProcessorRunner alarmRunner = entry.getValue();
			Thread alarmThread = entry.getKey();
			
			if (!alarmRunner.shouldStop() && !alarmThread.isAlive()) {
				logger.warn(String.format("Thread [%s] is not alive, restart it", alarmThread.getName()));
				alarmThread.start();
			}
		}
	}
	
}
