package org.binggo.apiwatchdog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import org.binggo.apiwatchdog.mapper.ApiStatDataMapper;

/**
 * <p>StatisService is a service for providing the statistical data from Redis and MySQL.</p>
 * <p>You cann't modify the statistical data here.</p>
 * @author Binggo
 */
@Service
public class StatisService {
	
	private static final Logger logger = LoggerFactory.getLogger(StatisService.class);
	
	@Autowired
	private ApiStatDataMapper apiStatDataMapper;
	
	@Autowired
	private StringRedisTemplate template;
	

}
