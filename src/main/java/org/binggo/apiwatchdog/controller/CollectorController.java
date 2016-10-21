package org.binggo.apiwatchdog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import org.binggo.apiwatchdog.collector.Collector;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.common.WatchdogResponse;
import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * provide the entry to send api call information through HTTP, but it is just an optional way.
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/collector")
public class CollectorController {
	
	private static final Logger logger = LoggerFactory.getLogger(CollectorController.class);
	
	@Autowired
	private Collector collector;
	
	@RequestMapping(method=RequestMethod.POST, value="/collect")
	public WatchdogResponse receiveApiCall(@RequestBody ApiCall apiCall) {
		String callUuid = Arrays.toString(apiCall.getCallUuid());
		logger.debug(String.format("receive an api call: %s", callUuid));
		
		try {
			collector.collect(apiCall);
			return WatchdogResponse.OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return new WatchdogResponse(ex.getReturnCode());
		}
	}

}
