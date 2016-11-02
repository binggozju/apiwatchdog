package org.binggo.apiwatchdog.processor.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
	
	private StringRedisTemplate stringRedisTemplate;
	
	private Integer keysExpireTime;

	@Autowired
	public AnalyzerProcessor(WatchdogEnv env, StringRedisTemplate stringRedisTemplate) {
		super(AnalyzerUtils.PROCESSOR_NAME);
		
		capacity = env.getInteger(AnalyzerUtils.QUEUE_CAPACITY_CONFIG, AnalyzerUtils.QUEUE_CAPACITY_DEFAULT);
		processorNum = env.getInteger(AnalyzerUtils.ANALYZER_THREAD_NUM_CONFIG, AnalyzerUtils.ANALYZER_THREAD_NUM_DEFAULT);
		keysExpireTime = env.getInteger(AnalyzerUtils.REDIS_KEYS_EXPIRE_CONFIG, AnalyzerUtils.REDIS_KEYS_EXPIRE_DEFAULT);
		
		this.stringRedisTemplate = stringRedisTemplate;
	}
	
	@Override
	protected void doInitialize() {
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
		
		// create the redis key and set the expire time
		stringRedisTemplate.watch(redisKeyName);
		stringRedisTemplate.multi();
		if(!stringRedisTemplate.hasKey(redisKeyName)) {
			stringRedisTemplate.opsForHash().putAll(redisKeyName, AnalyzerUtils.getDefaultHash());
			stringRedisTemplate.expire(redisKeyName, keysExpireTime, TimeUnit.MINUTES);
		} 
		stringRedisTemplate.exec();  // check the result, if fail, the key has exists.
		
		// update the statiscal information
		stringRedisTemplate.multi();
		stringRedisTemplate.boundHashOps(redisKeyName).increment(AnalyzerUtils.KEY_COUNT_TOTAL, 1);
		// TODO
		stringRedisTemplate.exec();	
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
