package org.binggo.apiwatchdog.processor.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * <p>THe AnalyzerProcessor is aimed to process all the API call events, 
 * and update the statistical information of all API in each API providers.</p>
 * @author Binggo
 */
@Component("analyzerProcessor")
public class AnalyzerProcessor extends WatchdogProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzerProcessor.class);
	
	private StringRedisTemplate template;
	
	@SuppressWarnings("unused")
	private Integer keysExpireTime;

	@Autowired
	public AnalyzerProcessor(WatchdogEnv env, StringRedisTemplate template) {
		super(AnalyzerUtils.PROCESSOR_NAME);
		
		capacity = env.getInteger(AnalyzerUtils.QUEUE_CAPACITY_CONFIG, AnalyzerUtils.QUEUE_CAPACITY_DEFAULT);
		processorNum = env.getInteger(AnalyzerUtils.ANALYZER_THREAD_NUM_CONFIG, AnalyzerUtils.ANALYZER_THREAD_NUM_DEFAULT);
		
		this.template = template;
	}
	
	@Override
	protected void doInitialize() {
		//template.setEnableTransactionSupport(true);
		// nothing to do
	}
	
	@Override
	protected boolean isPermitted(Event event) {
		// always return true for processing every API call event
		return true;
	}
	
	@Override
	public void close() {
		super.close();
	}
	
	@Override
	protected void processEvent(Event event) {
		ApiCall apiCall = (ApiCall) event.getBody();
		
		// get the key name of corresponding time slice in redis server
		if (apiCall.getRequestTime() == null) {
			logger.error(String.format("the api call event [%s] needs the request time", apiCall.getCallUuid()));
			return;
		}
		String redisKeyName = AnalyzerUtils.getRedisKeyName(apiCall.getApiId(), apiCall.getRequestTime());
		
		// update the statistical information in current time slice
		template.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_COUNT_TOTAL, 1);
		
		// Deprecated: cann't guarantee that the expire command must be executed in concurrent environment.
		// we adopt the manual way to remove the old keys in redis.
		/*int current_total = (int) template.boundHashOps(redisKeyName).get(AnalyzerUtils.KEY_COUNT_TOTAL);
		if (current_total == 1) {
			template.expire(redisKeyName, keysExpireTime, TimeUnit.MINUTES);
		}*/
		
		if (apiCall.getResponseTime() == null) {
			template.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_COUNT_TIMEOUT, 1);
		} else {
			int respTime = (int)(apiCall.getResponseTime().getTime() - apiCall.getRequestTime().getTime())/1000;
			if (respTime < 0) {
				logger.error(String.format("response time error: response time is less than request time in Api call [%s]", 
						apiCall.getCallUuid()));
				template.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_COUNT_TOTAL, -1);
				return;
			}
			
			template.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_RESPTIME_TOTAL, respTime);
			template.boundHashOps(redisKeyName).increment(AnalyzerUtils.getRespTimeKey(respTime), 1);
			
			if (!apiCall.getHttpReponseCode().equals("200")) {
				template.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_COUNT_NOT200, 1);
			} else if (!apiCall.getApiReturnCode().equals("0")) {
				template.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_200_NOT0, 1);
			}
			
		}
	}

	@Scheduled(initialDelay=1000, fixedDelay=3000)
	@Override
	public void runTimerTask() {
		if (!isInitialized()) {
			logger.warn(String.format("%s has not been initialized, wait", getName()));
			return;
		}
		
		createAndCheckProcessors();
	}
}
