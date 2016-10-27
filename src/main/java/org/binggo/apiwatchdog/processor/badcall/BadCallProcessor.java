package org.binggo.apiwatchdog.processor.badcall;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.mapper.ApiBadCallMapper;

/**
 * <p>Here are the bad calls:</p>
 * <p>1. A API call which has no response (timeout in other words).</p>
 * <p>2. A API call whose response code of HTTP is not 200.</p>
 * <p>3. A API call whose return code of API is not 0.</p>
 */
@Component("badCallProcessor")
public class BadCallProcessor extends WatchdogProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(BadCallProcessor.class);
	
	private static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.badcall.queue.capacity";
	private static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	
	private static final String PROCESSOR_NAME = "Badcall-Processor";
	private static final Integer PROCESSOR_THREAD_NUM = 1;
	
	// if the bad call queue is empty and collector thread is idle, make it sleep for a while
	private static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	private Integer capacity;
	
	@Autowired
	private volatile ApiBadCallMapper apiBadCallMapper;
	
	@Autowired
	public BadCallProcessor(WatchdogEnv env) {
		super(PROCESSOR_NAME);
		
		capacity = env.getInteger(QUEUE_CAPACITY_CONFIG, QUEUE_CAPACITY_DEFAULT);
		processQueue = new LinkedBlockingQueue<Event>(capacity);
	}
	
	@Override
	public void initialize() {
		super.initialize();
	}

	protected Boolean isPermitted(Event event) {
		return isBadCall((ApiCall) event.getBody());
	}
	
	private Boolean isBadCall(ApiCall apiCall) {
		if (apiCall == null) {
			return true;
		}
		
		// An API call which has no response
		if (apiCall.getRequestTime() == null) {
			return true;
		}
		// An API call whose response code of HTTP is not 200
		if (apiCall.getHttpReponseCode() != "200") {
			return true;
		}
		// An API call whose return code of API is not 0
		if (apiCall.getApiReturnCode() != "0") {
			return true;
		}
		
		return false;
	}
	
	@Override
	public void close() {
		super.close();
	}
	
	/**
	 * store all the bad API call information from to MySQL
	 */
	@Override
	public void process() {
		Event event = processQueue.poll();
		if (event == null) {
			try {
				Thread.sleep(IDLE_SLEEP_TIME*1000);
			} catch (InterruptedException ex) {
				logger.warn(ex.getMessage());
			}
			return;
		}
	
		// store the bad call to MySQL
		apiBadCallMapper.insert((ApiCall) event.getBody()); 
	}
	
	@Scheduled(initialDelay=1000, fixedDelay = 3000)
	@Override
	public void runTimerTask() {
		if (!isInitialized()) {
			logger.warn(String.format("%s has not been initialized, wait", getName()));
			return;
		}
		if (apiBadCallMapper == null) {
			logger.warn(String.format("ApiBadCallMapper has not been injected to %s, wait", getName()));
			return;
		}
		
		if (runnerMap.size() == 0) {
			ProcessorRunner badCallRunner = new ProcessorRunner(this);
			
			for (int i = 0; i < PROCESSOR_THREAD_NUM; i++) {
				Thread badCallThread = new Thread(badCallRunner);
				
				badCallThread.setName(String.format("%s-%d", PROCESSOR_NAME, i));
				
				runnerMap.put(badCallThread, badCallRunner);
				logger.info(String.format("start the bad call processor thread [%s]", badCallThread.getName()));
				badCallThread.start();
			}
			
			return;
		}
		
		for (Map.Entry<Thread, ProcessorRunner> entry : runnerMap.entrySet()) {
			ProcessorRunner badCallRunner = entry.getValue();
			Thread badCallThread = entry.getKey();
			
			if (!badCallRunner.shouldStop() && !badCallThread.isAlive()) {
				logger.warn(String.format("Thread [%s] is not alive, restart it", badCallThread.getName()));
				badCallThread.start();
			}
			
		}
	}

}