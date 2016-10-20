package org.binggo.apiwatchdog.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;

public class ProcessorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessorFactory.class);
	
	public Processor create(ProcessorType type) throws WatchdogException {
		logger.info(String.format("Creating instance of processor [%s]", type.getProcessorClassName()));
		
		Class<Processor> processorClass = getClass(type);
		
		try {
			return processorClass.newInstance();
		} catch (Exception ex) {
			throw new WatchdogException(ReturnCode.FAIL_CREATE_PROCESSOR, 
					String.format("Unable to load processor [%s]", type.getProcessorClassName()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Class<Processor> getClass(ProcessorType type) throws WatchdogException {
		try {
			return (Class<Processor>) Class.forName(type.getProcessorClassName());
		} catch (Exception ex) {
			throw new WatchdogException(ReturnCode.FAIL_LOAD_PROCESSOR, 
					String.format("Unable to load processor [%s]", type.getProcessorClassName()));
		}
	}

}
