package org.binggo.apiwatchdog.processor;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.processor.alarm.AlarmProcessor;
import org.binggo.apiwatchdog.processor.analyzer.AnalyzerProcessor;
import org.binggo.apiwatchdog.processor.badcall.BadCallProcessor;

@Component
public class ProcessorFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessorFactory.class);
	
	private ApplicationContext context;
	
	private static Map<ProcessorType, Processor> processorMap;
	
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
	
	/**
	 * get all the available Processors from the ApplicationContext
	 * @return
	 */
	public Map<ProcessorType, Processor> getAllProcessors() {
		if (processorMap == null) {
			synchronized (processorMap) {
				if (processorMap == null) {
					processorMap = Maps.newHashMap();
					
					Processor alarmProcessor = (AlarmProcessor) context.getBean("alarmProcessor");
					processorMap.put(ProcessorType.ALARM, alarmProcessor);
					Processor analyzerProcessor = (AnalyzerProcessor) context.getBean("analyzerProcessor");
					processorMap.put(ProcessorType.ANALYZER, analyzerProcessor);
					Processor badCallProcessor = (BadCallProcessor) context.getBean("badCallProcessor");
					processorMap.put(ProcessorType.BADCALL, badCallProcessor);
				}	
			}	
		}
		return processorMap;
	}

}
