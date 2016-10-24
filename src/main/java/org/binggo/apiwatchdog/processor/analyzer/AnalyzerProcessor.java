package org.binggo.apiwatchdog.processor.analyzer;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.binggo.apiwatchdog.config.Config;
import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.processor.Event;
import org.binggo.apiwatchdog.processor.Processor;

@Component("analyzerProcessor")
public class AnalyzerProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(AnalyzerProcessor.class);
	
	@Autowired
	private Config config;
	
	private LinkedBlockingQueue<Event> analyzerQueue;

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(ApiCall apiCall) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(List<ApiCall> apiCallList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
