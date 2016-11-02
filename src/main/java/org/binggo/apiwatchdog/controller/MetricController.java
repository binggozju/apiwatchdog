package org.binggo.apiwatchdog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.binggo.apiwatchdog.service.MetricService;

/**
 * 
 * @author Binggo
 */
@RestController
@RequestMapping("/metric")
public class MetricController {
	
	private static final Logger logger = LoggerFactory.getLogger(MetricController.class);
	
	@Autowired
	private MetricService metricService;

}
