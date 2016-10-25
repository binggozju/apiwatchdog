package org.binggo.apiwatchdog.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import org.binggo.apiwatchdog.WatchdogEvent;
import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.mapper.ApiItemMapper;
import org.binggo.apiwatchdog.mapper.ApiProviderMapper;
import org.binggo.apiwatchdog.processor.alarm.AlarmTemplate;

/**
 * Config is used to manage the configuration of API providers and corresponding API.
 * @author Administrator
 *
 */
@Component
public class Config {
	
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	
	@Autowired
	private ApiProviderMapper apiProviderMapper;
	@Autowired
	private ApiItemMapper apiItemMapper;
	
	private ReadWriteLock confRWLock;
	// providerId -> ProviderConfiguration
	private volatile Map<Integer, ProviderConfiguration> providerConfMap;
	// apiId -> ApiConfiguration
	private volatile Map<Integer, ApiConfiguration> apiConfMap;
	
	private Map<Integer, ProviderConfiguration> providerConfCacheMap;
	private Map<Integer, ApiConfiguration> apiConfCacheMap;
	
	public Config() {
		confRWLock = new ReentrantReadWriteLock();
		
		providerConfMap = new HashMap<Integer, ProviderConfiguration>();
		apiConfMap = new HashMap<Integer, ApiConfiguration>();
		
		providerConfCacheMap = new HashMap<Integer, ProviderConfiguration>();
		apiConfCacheMap = new HashMap<Integer, ApiConfiguration>();
	}
	
	/**
	 * <p>If the apiCall need to be alarmed, generate a WatchdogEvent and add the alarm receivers
	 * to the header of it.</p>
	 * <p>if not, return null.</p>
	 * @param apiCall
	 * @return
	 */
	public WatchdogEvent generateAlarmEvent(ApiCall apiCall) {
		WatchdogEvent event = null;
		Integer apiId = apiCall.getApiId();
		
		confRWLock.readLock().lock();
		
		Integer providerId = apiConfMap.get(apiId).getProviderId();
		String alarmType = apiConfMap.get(apiId).getAlarmType().toString();
		String weixinReceivers = providerConfMap.get(providerId).getWeixinReceivers();
		String mailReceivers = providerConfMap.get(providerId).getMailReceivers();
		String phoneReceivers = providerConfMap.get(providerId).getPhoneReceivers();
		
		event = genInitialAlarmEvent(apiCall);
		confRWLock.readLock().unlock();
		
		if (event != null) {
			event.addHeader(AlarmTemplate.ALARM_TYPE_KEY, alarmType);
			event.addHeader(AlarmTemplate.WEIXIN_RECEIVERS_KEY, weixinReceivers);
			event.addHeader(AlarmTemplate.MAIL_RECEIVERS_KEY, mailReceivers);
			event.addHeader(AlarmTemplate.SMS_RECEIVERS_KEY, phoneReceivers);
		}
		return event;
	}
	
	private WatchdogEvent genInitialAlarmEvent(ApiCall apiCall) {
		Integer apiId = apiCall.getApiId();
		Integer providerId = apiConfMap.get(apiId).getProviderId();
		
		WatchdogEvent initialEvent = null;
		
		if (providerConfMap.get(providerId).getState() == 0) {
			return initialEvent;
		}
		if (apiConfMap.get(apiId).getState() == 0) {
			return initialEvent;
		}
		
		// check whether there is a response
		if (apiCall.getResponseTime() == null) {
			initialEvent = WatchdogEvent.buildEvent(apiCall);
			initialEvent.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_NO_RESPONSE);
			return initialEvent;
		}
		// check the response time
		int timeDelta = (int)(apiCall.getResponseTime().getTime() - apiCall.getRequestTime().getTime())/1000;
		if (timeDelta >= apiConfMap.get(apiId).getMetricResptimeThreshold()) {
			initialEvent = WatchdogEvent.buildEvent(apiCall);
			initialEvent.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_EXCEED_THRESHOLD);
			return initialEvent;
		}
		// check the response code of HTTP
		if (apiCall.getHttpReponseCode() != "200" && apiConfMap.get(apiId).getMetricNot200() !=0) {
			initialEvent = WatchdogEvent.buildEvent(apiCall);
			initialEvent.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_NOT_HTTP200);
			return initialEvent;
		}
		// check the return code
		/*if (apiCall.getApiReturnCode() != "0" && apiConfMap.get(apiId).getMetric200Not0() !=0) {
			initialEvent = WatchdogEvent.buildEvent(apiCall);
			initialEvent.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_NOT_RETCODE0);
			return initialEvent;
		}*/
		
		return initialEvent;
	}
	
	
	/**
	 * refresh the configuration of API providers and API from MySQL
	 */
	@Scheduled(fixedDelay = 5*60*1000)
	public void refreshConfigData() {
		// get the new configuration for providers
		List<ProviderConfiguration> providerConfList = apiProviderMapper.listProviderConf();
		if (providerConfList == null) {
			logger.warn("there is no provider configuration");
			providerConfList = Lists.newArrayList();
		}
		for (ProviderConfiguration pConf : providerConfList) {
			Integer providerId = pConf.getProviderId();
			providerConfCacheMap.put(providerId, pConf);
		}
		
		// get the new configuration for API
		List<ApiConfiguration> apiConfList = apiItemMapper.listApiConf();
		if (apiConfList == null) {
			logger.warn("there is no API configuration");
			apiConfList = Lists.newArrayList();
		}
		for (ApiConfiguration aConf : apiConfList) {
			Integer apiId = aConf.getApiId();
			apiConfCacheMap.put(apiId, aConf);
		}
		
		// refresh the configuration
		confRWLock.writeLock().lock();
		providerConfMap.clear();
		providerConfMap.putAll(providerConfCacheMap);
		apiConfMap.clear();
		apiConfMap.putAll(apiConfCacheMap);
		confRWLock.writeLock().unlock();
		
		providerConfCacheMap.clear();
		apiConfCacheMap.clear();
		
		logger.info("refresh the configuration successfully.");
	}
}
