package org.binggo.apiwatchdog.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.mapper.ApiProviderMapper;
import org.binggo.apiwatchdog.mapper.ApiItemMapper;
import org.binggo.apiwatchdog.domain.ApiProvider;
import org.binggo.apiwatchdog.domain.ApiItem;

/**
 * manage the configuration information about the API providers (service) and their API.
 */
@Service
public class ConfigService {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
	
	@Autowired
	private ApiProviderMapper apiProviderMapper;
	
	@Autowired
	private ApiItemMapper apiItemMapper;
	
	// manage the API providers
	public void addApiProvider(ApiProvider apiProvider) throws WatchdogException {
		if (apiProvider == null || apiProvider.getName() == null || apiProvider.getWeixinReceivers() == null) {
			throw new WatchdogException(ReturnCode.INVALID_PARAMETER);
		}
		
		apiProviderMapper.insert(apiProvider);
	}
	
	public void deleteApiProvider(Integer providerId) throws WatchdogException {
		if (providerId == null) {
			throw new WatchdogException(ReturnCode.INVALID_PARAMETER, 
					"providerId for deleteApiProvider is null");
		}
		
		apiProviderMapper.deleteById(providerId);
	}
	
	public void updateApiProvider(ApiProvider apiProvider) throws WatchdogException {
		if (apiProvider == null || apiProvider.getProviderId() == null) {
			throw new WatchdogException(ReturnCode.INVALID_PARAMETER, 
					"providerId for updateApiProvider is null");
		}
		
		apiProviderMapper.updateById(apiProvider);
	}
	
	public List<ApiProvider> listApiProviders() {
		List<ApiProvider> apiProviderList = apiProviderMapper.listApiProviders();
		
		if (apiProviderList.size() == 0) {
			logger.warn("there is no api provider registered so far");
		}
		
		return apiProviderList;
	}
	
	// manage the API
	public void addApiItem(ApiItem api) throws WatchdogException {
		if (api == null || api.getName() == null) {
			throw new WatchdogException(ReturnCode.INVALID_PARAMETER);
		}
		
		apiItemMapper.insert(api);
	}
	
	public void deleteApiItem(Integer apiId) throws WatchdogException {
		if (apiId == null) {
			throw new WatchdogException(ReturnCode.INVALID_PARAMETER, 
					"apiId for deleteApiItem is null");
		}

		apiItemMapper.deleteById(apiId);
	}
	
	public void updateApiItem(ApiItem api) throws WatchdogException {
		if (api == null || api.getApiId() == null) {
			throw new WatchdogException(ReturnCode.INVALID_PARAMETER, 
					"apiId for updateApiItem is null");
		}
		
		apiItemMapper.updateById(api);
	}
	
	public List<ApiItem> listApiItems(Integer providerId) {
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		
		if (apiItemList.size() == 0) {
			logger.warn(String.format("there is no api registered for api provider [providerId=%d]", providerId));
		}

		return apiItemList;
	}
	
	public List<ApiItem> listApiItems() {
		List<ApiItem> apiItemList = apiItemMapper.listApiItems();
		
		if (apiItemList.size() == 0) {
			logger.warn("there is no api registered so far");
		}
		
		return apiItemList;
	}
	
}
