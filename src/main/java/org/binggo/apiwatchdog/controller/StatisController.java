package org.binggo.apiwatchdog.controller;

import org.binggo.apiwatchdog.statis.Statis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Binggo
 */
@RestController
@RequestMapping("/statis")
public class StatisController {
	
	private static final Logger logger = LoggerFactory.getLogger(StatisController.class);
	
	@Autowired
	private Statis statis;
	
	
	
	
	
	

}
