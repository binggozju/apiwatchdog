package org.binggo.apiwatchdog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.binggo.apiwatchdog.Collector;
import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.common.WatchdogResponse;
import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * provide the entry to send the event (API call information) through HTTP, but it is just an optional way.
 * @author Binggo
 */
@RestController
@RequestMapping("/collector")
public class CollectorController {
	
	private static final Logger logger = LoggerFactory.getLogger(CollectorController.class);

	private Collector collector;
	
	@Autowired
	public CollectorController(@Qualifier("simpleCollector") Collector collector) {
		this.collector = collector;
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/collect")
	public WatchdogResponse receiveApiCall(@RequestBody ApiCall apiCall) {
		logger.debug(String.format("receive an api call: %s", apiCall.getCallUuid()));
		
		try {
			collector.collect(Event.buildEvent(apiCall));
			return WatchdogResponse.SIMPLE_OK_RESPONSE;
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}

}
