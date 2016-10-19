package org.binggo.apiwatchdog.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	public void addApiProvider(ApiProvider apiProvider) {
		// TODO
	}
	
	public void deleteApiProvider(Integer providerId) {
		// TODO
	}
	
	public void updateApiProvider(ApiProvider apiProvider) {
		// TODO
	}
	
	public List<ApiProvider> listApiProviders() {
		// TODO
		return null;
	}
	
	// manage the API
	public void addApiItem(ApiItem api) {
		// TODO
	}
	
	public void deleteApiItem(Integer apiId) {
		// TODO
	}
	
	public void updateApiItem(ApiItem api) {
		// TODO
	}
	
	public List<ApiItem> listApiItems(Integer providerId) {
		// TODO
		return null;
	}
	
	public List<ApiItem> listApiItems() {
		// TODO
		return null;
	}
	
}
