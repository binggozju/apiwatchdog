package org.binggo.apiwatchdog.processor.analyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.config.Config;

@Component("analyzerProcessor")
public class AnalyzerProcessor extends WatchdogProcessor {
	
	public AnalyzerProcessor() {
		super("");
		// TODO Auto-generated constructor stub
	}

	private static final Logger logger = LoggerFactory.getLogger(AnalyzerProcessor.class);
	
	@Autowired
	private Config config;

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runTimerTask() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isPermitted(Event event) {
		// TODO Auto-generated method stub
		return false;
	}
	


}
