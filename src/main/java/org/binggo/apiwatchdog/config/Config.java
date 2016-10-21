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

import org.binggo.apiwatchdog.mapper.ApiItemMapper;
import org.binggo.apiwatchdog.mapper.ApiProviderMapper;

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
	
	// TODO: get configuration with read lock
	
	
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
