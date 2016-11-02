package org.binggo.apiwatchdog.controller;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;

import org.binggo.apiwatchdog.common.PageInfoExt;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.common.WatchdogResponse;
import org.binggo.apiwatchdog.domain.ApiItem;
import org.binggo.apiwatchdog.domain.ApiProvider;
import org.binggo.apiwatchdog.service.ConfigService;

/**
 * <p>
 * The ConfigController provides some API for users to register and manage their API providers and API.
 * </p>
 * Here is the configuration information which can be managed as following:
 * <p>1. the detailed information about the API providers;</p>
 * <p>2. the API list and their alarm metrics for each API provider.</p>
 *
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
	
	@Autowired
	private ConfigService configService;
	
	// API provider
	@RequestMapping(method=RequestMethod.POST, value="/provider/add")
	public WatchdogResponse registerApiProvider(@RequestBody ApiProvider apiProvider) {
		logger.debug("receive a post request to /provider/add");
		
		try {
			configService.addApiProvider(apiProvider);
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value="/provider/{providerId}")
	public WatchdogResponse removeApiProvider(@PathVariable("providerId") int providerId) {
		logger.debug(String.format("receive a delete request to /provider/%d", providerId));
		
		try {
			configService.deleteApiProvider(providerId);
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/provider/update")
	public WatchdogResponse updateApiProvider(@RequestBody ApiProvider apiProvider) {
		logger.debug("receive a post request to /provider/update");
		
		try {
			configService.updateApiProvider(apiProvider);
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * list all API providers supporting paging and not paging
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET, value="/provider/list")
	public WatchdogResponse listApiProviders(@RequestParam Map<String, String> params) {
		logger.info("receive a get request to /provider/list");
		
		String offset = params.get("offset");
		String size = params.get("size");
		
		// without paging
		if (offset == null && size == null) {
			List<ApiProvider> apiProviderList = configService.listApiProviders();
			return WatchdogResponse.getResponse(ReturnCode.OK, apiProviderList);
		}
		
		// paging
		offset = offset == null ? "0": offset;
		size = size == null ? "10": size;
		PageHelper.startPage(Integer.parseInt(offset), Integer.parseInt(size));
		
		List<ApiProvider> apiProviderList = configService.listApiProviders();
		PageInfoExt<ApiProvider> apiProviderPage = new PageInfoExt<ApiProvider>(apiProviderList);
		return WatchdogResponse.getResponse(ReturnCode.OK, apiProviderPage);
	}
	
	// API
	@RequestMapping(method=RequestMethod.POST, value="/api/add")
	public WatchdogResponse registerApi(@RequestBody ApiItem apiItem) {
		logger.debug("receive a post request to /api/add");
		
		try {
			configService.addApiItem(apiItem);
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value="/api/{apiId}")
	public WatchdogResponse removeApi(@PathVariable("apiId") int apiId) {
		logger.debug(String.format("receive a delete request to /api/%d", apiId));
		
		try {
			configService.deleteApiItem(apiId);
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/api/update")
	public WatchdogResponse updateApi(@RequestBody ApiItem apiItem) {
		logger.debug("receive a post request to /api/update");
		
		try {
			configService.updateApiItem(apiItem);
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * <p>listAllApi support both paging and not paging.</p>
	 * <p>listAllApi will list all the API registered before. if you provide a parameter
	 * named "providerId", it will list all the API of corresponding API provider.
	 * </p>
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET, value="/api/list")
	public WatchdogResponse listAllApi(@RequestParam Map<String, String> params) {
		logger.info("receive a get request to /api/list");
		
		String offset = params.get("offset");
		String size = params.get("size");
		String providerId = params.get("providerId");
		
		List<ApiItem> apiItemList;
		// without paging
		if (offset == null && size == null) {
			if (providerId == null) {
				apiItemList = configService.listApiItems();
				return WatchdogResponse.getResponse(ReturnCode.OK, apiItemList);
			} else {
				apiItemList = configService.listApiItems(Integer.parseInt(providerId));
				return WatchdogResponse.getResponse(ReturnCode.OK, apiItemList);
			}	
		}
		
		// paging
		offset = offset == null ? "0": offset;
		size = size == null ? "10": size;
		PageHelper.startPage(Integer.parseInt(offset), Integer.parseInt(size));
		
		if (providerId == null) {
			apiItemList = configService.listApiItems();
			PageInfoExt<ApiItem> apiItemPage = new PageInfoExt<ApiItem>(apiItemList);
			return WatchdogResponse.getResponse(ReturnCode.OK, apiItemPage);	
		} else {
			apiItemList = configService.listApiItems(Integer.parseInt(providerId));
			PageInfoExt<ApiItem> apiItemPage = new PageInfoExt<ApiItem>(apiItemList);
			return WatchdogResponse.getResponse(ReturnCode.OK, apiItemPage);
		}
	}
}
