package org.binggo.apiwatchdog.processor.badcall;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.config.Config;
import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.mapper.ApiBadCallMapper;

/**
 * <p>Here are the bad calls:</p>
 * <p>1. A API call which has no response (timeout in other words).</p>
 * <p>2. A API call whose response code of HTTP is not 200.</p>
 * <p>3. A API call whose return code of API is not 0.</p>
 */
@Component("badCallProcessor")
public class BadCallProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(BadCallProcessor.class);
	
	private static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.badcall.queue.capacity";
	private static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	
	private Integer queueCapacity;
	private LinkedBlockingQueue<ApiCall> badCallQueue;
	
	@SuppressWarnings("unused")
	@Autowired
	private Config config;
	
	@Autowired
	private ApiBadCallMapper apiBadCallMapper;
	
	// if the bad call queue is empty and collector thread is idle, make it sleep for a while
	private static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	private static final String BADCALL_THREAD_NAME = "Badcall-Thread";
	private AtomicInteger badCallThreadId = new AtomicInteger(0);
	private Thread badCallThread;
	
	@Autowired
	public BadCallProcessor(WatchdogEnv env) {
		queueCapacity = env.getInteger(QUEUE_CAPACITY_CONFIG, QUEUE_CAPACITY_DEFAULT);
		badCallQueue = new LinkedBlockingQueue<ApiCall>(queueCapacity);
	}

	@Override
	public void initialize() {
		logger.info("The BadCallProcessor has been initialized.");
	}

	/**
	 * put all the bad API call to the badCallQueue.
	 */
	@Override
	public void process(ApiCall apiCall) {
		if (!isBadCall(apiCall)) {
			return;
		}
		// must not block here
		if (!badCallQueue.offer(apiCall)) {
			String callUuid = Arrays.toString(apiCall.getCallUuid());
			logger.error(String.format("fail to offer api call [%s] to bad call queue.", callUuid));
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
		logger.info("The BadCallProcessor has been closed.");
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
	
	/**
	 * store all the bad API call information from the blocking queue to MySQL
	 */
	@Scheduled(fixedDelay = 5*1000)
	public void dump() {
		// configure the bad call thread
		badCallThread = Thread.currentThread();
		String threadName = String.format("%s-%d", BADCALL_THREAD_NAME, badCallThreadId.get());
		badCallThread.setName(threadName);
		badCallThreadId.getAndIncrement();
		logger.info(String.format("start bad call thread [%s]", threadName));
		
		while (true) {
			try {
				ApiCall apiCall = badCallQueue.poll();
				if (apiCall == null) {
					Thread.sleep(IDLE_SLEEP_TIME*1000);
					continue;
				}
				
				// store the bad call to MySQL
				apiBadCallMapper.insert(apiCall);
			
			} catch (InterruptedException ex) {
				logger.error(String.format("the bad call thread [%d] has been interrupted", threadName));
				return;
			}
		}	
	}

}
