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

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.TimerRunnable;
import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.mapper.ApiItemMapper;
import org.binggo.apiwatchdog.mapper.ApiProviderMapper;
import org.binggo.apiwatchdog.processor.alarm.AlarmTemplate;

/**
 * ConfigProvider is used to manage the configuration of API providers and corresponding API.
 * @author Administrator
 *
 */
@Component
public class ConfigProvider implements TimerRunnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);
	
	@Autowired
	private volatile ApiProviderMapper apiProviderMapper;
	@Autowired
	private volatile ApiItemMapper apiItemMapper;
	
	private ReadWriteLock confRWLock;
	// providerId -> ProviderConfiguration
	private volatile Map<Integer, ProviderConfiguration> providerConfMap;
	// apiId -> ApiConfiguration
	private volatile Map<Integer, ApiConfiguration> apiConfMap;
	
	private Map<Integer, ProviderConfiguration> providerConfCacheMap;
	private Map<Integer, ApiConfiguration> apiConfCacheMap;
	
	public ConfigProvider() {
		confRWLock = new ReentrantReadWriteLock();
		
		providerConfMap = new HashMap<Integer, ProviderConfiguration>();
		apiConfMap = new HashMap<Integer, ApiConfiguration>();
		
		providerConfCacheMap = new HashMap<Integer, ProviderConfiguration>();
		apiConfCacheMap = new HashMap<Integer, ApiConfiguration>();
	}
	
	/**
	 * <p>if the api call contained in the given event should be alarmed, 
	 * add helpful information such as alarm receivers to the headers of this given event, 
	 * and return true finally.</p>
	 * <p>otherwise return false.</p>
	 * @param event
	 * @return whether or not to send alarm message for the given event.
	 */
	public Boolean shouldAlarm(Event event) {
		ApiCall apiCall = (ApiCall) event.getBody();
		Integer apiId = apiCall.getApiId();
		
		confRWLock.readLock().lock();
		Integer providerId = apiConfMap.get(apiId).getProviderId();
		
		// add helpful information to the headers
		String alarmType = apiConfMap.get(apiId).getAlarmType().toString();
		event.addHeader(AlarmTemplate.ALARM_TYPE_KEY, alarmType);
		
		
		String weixinReceivers = apiConfMap.get(apiId).getWeixinReceivers();
		if (weixinReceivers == null || weixinReceivers.equals("")) {
			String defaultWeixinReceivers = providerConfMap.get(providerId).getWeixinReceivers();
			weixinReceivers = defaultWeixinReceivers;
		}
		event.addHeader(AlarmTemplate.WEIXIN_RECEIVERS_KEY, weixinReceivers);
		
		String mailReceivers = apiConfMap.get(apiId).getMailReceivers();
		if (mailReceivers == null || mailReceivers.equals("")) {
			String defaultMailReceivers = providerConfMap.get(providerId).getMailReceivers();
			mailReceivers = defaultMailReceivers;
		}
		event.addHeader(AlarmTemplate.MAIL_RECEIVERS_KEY, mailReceivers);
		
		String phoneReceivers = apiConfMap.get(apiId).getPhoneReceivers();
		if (phoneReceivers == null || phoneReceivers.equals("")) {
			String defaultPhoneReceivers = providerConfMap.get(providerId).getPhoneReceivers();
			phoneReceivers = defaultPhoneReceivers;
		}
		event.addHeader(AlarmTemplate.SMS_RECEIVERS_KEY, phoneReceivers);
		
		// check provider's state
		if (providerConfMap.get(providerId).getState() == 0) {
			confRWLock.readLock().unlock();
			return false;
		}
		// check API's state
		if (apiConfMap.get(apiId).getState() == 0) {
			confRWLock.readLock().unlock();
			return false;
		}
		// check whether there is a response
		if (apiCall.getResponseTime() == null) {
			if (apiConfMap.get(apiId).getMetricResptimeThreshold() == 0) {
				confRWLock.readLock().unlock();
				return false;
			} else {
				event.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_NO_RESPONSE);
				confRWLock.readLock().unlock();
				return true;
			}
		}
		// check the response time
		int timeDelta = (int)(apiCall.getResponseTime().getTime() - apiCall.getRequestTime().getTime())/1000;
		if (timeDelta >= apiConfMap.get(apiId).getMetricResptimeThreshold()) {
			if (apiConfMap.get(apiId).getMetricResptimeThreshold() > 0) {
				event.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_EXCEED_THRESHOLD);
				confRWLock.readLock().unlock();
				return true;
			}	
		}
		// check the response code of HTTP
		if (apiCall.getHttpResponseCode() != null && !apiCall.getHttpResponseCode().equals("200") 
				&& apiConfMap.get(apiId).getMetricNot200() != 0) {
			event.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_NOT_HTTP200);
			confRWLock.readLock().unlock();
			return true;
		}
		// check the return code
		/*if (apiCall.getApiReturnCode() != null && !apiCall.getApiReturnCode().equals("0") 
		 		&& apiConfMap.get(apiId).getMetric200Not0() != 0) {
			event.addHeader(AlarmTemplate.ALARM_REASON_KEY, AlarmTemplate.ALARM_REASON_NOT_RETCODE0);
			confRWLock.readLock().unlock();
			return true;
		}*/
		
		confRWLock.readLock().unlock();
		return false;
	}
	
	private void refreshConfigData() {
		// get the new configuration for providers
		List<ProviderConfiguration> providerConfList = apiProviderMapper.listProviderConf();
		if (providerConfList.size() == 0) {
			logger.warn("there is no provider configuration");
		}
		for (ProviderConfiguration pConf : providerConfList) {
			Integer providerId = pConf.getProviderId();
			providerConfCacheMap.put(providerId, pConf);
		}
		
		// get the new configuration for API
		List<ApiConfiguration> apiConfList = apiItemMapper.listApiConf();
		if (apiConfList.size() == 0) {
			logger.warn("there is no API configuration");
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

	/**
	 * refresh the configuration of API providers and API from MySQL
	 */
	@Scheduled(initialDelay=500, fixedDelay=3*60*1000)
	@Override
	public void runTimerTask() {
		while (apiProviderMapper == null || apiItemMapper == null) {
			try {
				logger.warn("apiProviderMapper and apiItemMapper have not been injected to"
						+ "Config by now, wait");
				Thread.sleep(200);
			} catch (InterruptedException ex) {
				//ex.printStackTrace();
			}
		}
		
		refreshConfigData();
	}
}
