package org.binggo.apiwatchdog.processor.alarm;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import org.binggo.apiwatchdog.domain.ApiCall;
import org.binggo.apiwatchdog.processor.Event;
import org.binggo.apiwatchdog.processor.Processor;

@Component("alarmProcessor")
public class AlarmProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(AlarmProcessor.class);
	
	private LinkedBlockingQueue<Event> alarmQueue;

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
