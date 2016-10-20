package org.binggo.apiwatchdog.processor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import org.binggo.apiwatchdog.domain.ApiCall;

@Component
public class FacadeProcessor implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(FacadeProcessor.class);
	
	//private ProcessorFactory processorFactory;
	
	private Map<ProcessorType, Processor> processors;
	
	@Autowired
	public FacadeProcessor() {
		
		processors = Maps.newHashMap();
		// TODO: get the processor instances from the context, and put them to processors.
		// do not use the ProcessorFactory because it isn't convenient		
	}

	@Override
	public void initialize() {
		for(Map.Entry<ProcessorType, Processor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.initialize();
		}
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
		for(Map.Entry<ProcessorType, Processor> entry : processors.entrySet()) {
			Processor processor = entry.getValue();
			processor.close();
		}
	}

}
