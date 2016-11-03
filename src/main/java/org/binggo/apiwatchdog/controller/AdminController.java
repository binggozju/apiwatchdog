package org.binggo.apiwatchdog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.binggo.apiwatchdog.common.WatchdogResponse;
import org.binggo.apiwatchdog.dump.DataDumper;
import org.binggo.apiwatchdog.service.AdminService;

/**
 * 
 * @author Binggo
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private DataDumper dataDumper;
	
	@RequestMapping(method=RequestMethod.GET, value="/dump/{timeSlice}")
	public WatchdogResponse dumpTimeSlice(@PathVariable("timeSlice") String timeSlice) {
		logger.debug(String.format("receive a get request to /admin/dump/%s", timeSlice));
		
		dataDumper.dump(timeSlice);
		return WatchdogResponse.SIMPLE_OK_RESPONSE;	
	}
	

}
