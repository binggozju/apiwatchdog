package org.binggo.apiwatchdog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * The ConfigController provides some API for users to register and manage their API providers and API.
 * </p>
 * Here is he configuration information which can be managed as following:
 * <p>1. the detailed information about the API providers;</p>
 * <p>2. the API list and their alarm metrics for each API provider.</p>
 *
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

}
