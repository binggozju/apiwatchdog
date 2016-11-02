package org.binggo.apiwatchdog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * <p>MetricsService provides an entrance to get the metrics of internal components in apiwatchdog
 * in real time, such as collector and processor.</p>
 * @author Binggo
 */
@Service
public class MetricService {
	
	private static final Logger logger = LoggerFactory.getLogger(MetricService.class);

}
