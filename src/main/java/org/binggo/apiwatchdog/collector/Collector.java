package org.binggo.apiwatchdog.collector;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.processor.Processor;

public class Collector {
	
	private static final Logger logger = LoggerFactory.getLogger(Collector.class);
	
	private LinkedBlockingQueue<ApiCall> collectQueue;
	
	private Processor processor;

}
