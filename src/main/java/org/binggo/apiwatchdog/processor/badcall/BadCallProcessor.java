package org.binggo.apiwatchdog.processor.badcall;

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
 * @author Binggo
 */
@Component("badCallProcessor")
public class BadCallProcessor extends WatchdogProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(BadCallProcessor.class);
	
	@Autowired
	private volatile ApiBadCallMapper apiBadCallMapper;
	
	@Autowired
	public BadCallProcessor(WatchdogEnv env) {
		super(BadCallConstants.PROCESSOR_NAME);
		
		capacity = env.getInteger(BadCallConstants.QUEUE_CAPACITY_CONFIG, BadCallConstants.QUEUE_CAPACITY_DEFAULT);
		processorNum = env.getInteger(BadCallConstants.BADCALL_THREAD_NUM_CONFIG, BadCallConstants.BADCALL_THREAD_NUM_DEFAULT);
	}
	
	@Override
	protected void doInitialize() {
		// nothing to do
	}

	protected boolean isPermitted(Event event) {
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
	protected void processEvent(Event event) {
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
		
		createAndCheckProcessors();
	}
}