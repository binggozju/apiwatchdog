package org.binggo.apiwatchdog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.binggo.apiwatchdog.service.StatisService;

/**
 * 
 * @author Binggo
 */
@RestController
@RequestMapping("/statis")
public class StatisController {
	
	private static final Logger logger = LoggerFactory.getLogger(StatisController.class);
	
	@Autowired
	private StatisService statisService;

}
