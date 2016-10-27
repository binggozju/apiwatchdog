package org.binggo.apiwatchdog.processor;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.WatchdogProcessor;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;

@Component
public class ProcessorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessorFactory.class);
	
	private ApplicationContext context;
	
	private Map<ProcessorType, WatchdogProcessor> processorMap;
	
	@Autowired
	public ProcessorFactory(ApplicationContext context) {
		this.context = context;
	}
	
	/**
	 * get the Processor through reflecting
	 * @param type the type of processor
	 * @return the processor instance
	 * @throws WatchdogException
	 */
	public Processor create(ProcessorType type) throws WatchdogException {
		logger.info(String.format("Creating instance of processor [%s]", type.getProcessorClassName()));
		
		Class<WatchdogProcessor> processorClass = getClass(type);
		
		try {
			return processorClass.newInstance();
		} catch (Exception ex) {
			throw new WatchdogException(ReturnCode.FAIL_CREATE_PROCESSOR, 
					String.format("Unable to load processor [%s]", type.getProcessorClassName()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public Class<WatchdogProcessor> getClass(ProcessorType type) throws WatchdogException {
		try {
			return (Class<WatchdogProcessor>) Class.forName(type.getProcessorClassName());
		} catch (Exception ex) {
			throw new WatchdogException(ReturnCode.FAIL_LOAD_PROCESSOR, 
					String.format("Unable to load processor [%s]", type.getProcessorClassName()));
		}
	}
	
	/**
	 * get all the available Processors from the ApplicationContext
	 * @return
	 */
	public Map<ProcessorType, WatchdogProcessor> getAllProcessors() {
		if (processorMap == null) {
			processorMap = Maps.newHashMap();
			
			WatchdogProcessor alarmProcessor = (WatchdogProcessor) context.getBean("alarmProcessor");
			processorMap.put(ProcessorType.ALARM, alarmProcessor);
			logger.debug("get AlarmProcessor");
			
			WatchdogProcessor analyzerProcessor = (WatchdogProcessor) context.getBean("analyzerProcessor");
			processorMap.put(ProcessorType.ANALYZER, analyzerProcessor);
			logger.debug("get AnalyzerProcessor");
			
			WatchdogProcessor badCallProcessor = (WatchdogProcessor) context.getBean("badCallProcessor");
			processorMap.put(ProcessorType.BADCALL, badCallProcessor);
			logger.debug("get BadCallProcessor");
		}
		return processorMap;
	}

}
