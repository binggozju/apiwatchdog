package org.binggo.apiwatchdog.processor;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.WatchdogProcessor;

@Component("facadeProcessor")
public class FacadeProcessor implements Processor {
	
	private ProcessorFactory processorFactory;
	private Map<ProcessorType, WatchdogProcessor> processors;
	
	@Autowired
	public FacadeProcessor(ProcessorFactory processorFactory) {
		this.processors = Maps.newHashMap();	
		this.processorFactory = processorFactory;
	}

	@Override
	public void initialize() {
		processors.putAll(processorFactory.getAllProcessors());
		
		// initialize the concrete processor module
		for(Map.Entry<ProcessorType, WatchdogProcessor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.initialize();
		}
	}
	
	@Override
	public void take(Event event) {
		for(Map.Entry<ProcessorType, WatchdogProcessor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.take(event);
		}
	}

	@Override
	public void take(List<Event> eventList) {
		for (Event event : eventList) {
			take(event);
		}
	}
	
	@Override
	public void close() {
		for(Map.Entry<ProcessorType, WatchdogProcessor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.close();
		}
	}

	@Override
	public void process() {
		// nothing to do
	}

}
