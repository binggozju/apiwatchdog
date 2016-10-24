package org.binggo.apiwatchdog.processor.alarm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.impl.client.CloseableHttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import org.binggo.apiwatchdog.WatchdogEvent;
import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.config.Config;
import org.binggo.apiwatchdog.domain.ApiCall;

@Component("alarmProcessor")
public class AlarmProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmProcessor.class);
	
	private static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.alarm.queue.capacity";
	private static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	private Integer queueCapacity;
	private LinkedBlockingQueue<WatchdogEvent> alarmQueue;
	
	// if the alarm queue is empty and collector thread is idle, make it sleep for a while
	private static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	private static final String ALARM_THREAD_NUM_CONFIG = "apiwatchdog.alarm.runner.num";
	private static final Integer ALARM_THREAD_NUM_DEFAULT = 2;
	private Integer alarmRunnerNum;
	private static final String ALARM_THREAD_NAME = "Alarm-Runner-Thread";
	private Map<Thread, AlarmRunner> alarmRunnerMap;
	
	@Autowired
	private Config config;
	
	private static final String WEIXIN_URL_CONFIG = "msgsender.weixin.url";
	private String weixinSenderUrl;
	private CloseableHttpClient httpClient;
	
	@Autowired
	public AlarmProcessor(WatchdogEnv env) {
		queueCapacity = env.getInteger(QUEUE_CAPACITY_CONFIG, QUEUE_CAPACITY_DEFAULT);
		alarmQueue = new LinkedBlockingQueue<WatchdogEvent>(queueCapacity);
		
		alarmRunnerNum = env.getInteger(ALARM_THREAD_NUM_CONFIG, ALARM_THREAD_NUM_DEFAULT);
		alarmRunnerMap = Maps.newHashMap();
		
		weixinSenderUrl = env.getString(WEIXIN_URL_CONFIG, "");
		httpClient = HttpClientUtils.getHttpClient();
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
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("The BadCallProcessor has been closed.");
	}
	

	
	/**
	 * send alarm messages according to the API call information in alarmQueue
	 */
	@Scheduled(fixedDelay = 5*1000)
	public void alarm() {
		// TODO
		// use multiple threads to send alarm message
	}
	
	/**
	 * support to send alarm messages with multiple threads
	 */
	private class AlarmRunner implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
