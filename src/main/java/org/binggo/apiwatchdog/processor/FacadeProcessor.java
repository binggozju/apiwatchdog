package org.binggo.apiwatchdog.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.domain.ApiCall;

@Component("facadeProcessor")
public class FacadeProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(FacadeProcessor.class);
	
	@Autowired
	private ProcessorFactory processorFactory;
	
	private Map<ProcessorType, Processor> processors;
	
	private AtomicBoolean initialized = new AtomicBoolean(false);
	
	public FacadeProcessor() {
		processors = Maps.newHashMap();	
	}

	@Override
	public void initialize() {
		if (initialized.get()) {
			logger.warn("the facade processor has already been initialized.");
			return;
		}
		// initialize the concrete processor module
		for(Map.Entry<ProcessorType, Processor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.initialize();
		}
		
		// initialize FacadeProcessor itself
		processors.putAll(processorFactory.getAllProcessors());
		
		initialized.set(true);
	}
	
	@Override
	public void process(ApiCall apiCall) {
		for(Map.Entry<ProcessorType, Processor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.process(apiCall);
		}
	}

	@Override
	public void process(List<ApiCall> apiCallList) {
		for(Map.Entry<ProcessorType, Processor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.process(apiCallList);
		}
	}

	@Override
	public void close() {
		for(Map.Entry<ProcessorType, Processor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.close();
		}
	}

}
