package org.binggo.apiwatchdog.processor;

public enum ProcessorType {
	
	/**
	 * processor used to send alarm messages.
	 * @see AlarmProcessor
	 */
	ALARM("org.binggo.apiwatchdog.processor.alarm.AlarmProcessor"),
	
	/**
	 * processor used to analyzer the statistical information for each API.
	 * @see AnalyzerProcessor
	 */
	ANALYZER("org.binggo.apiwatchdog.processor.analyzer.AnalyzerProcessor"),
	
	/**
	 * processor used to store the bad API call.
	 * @see BadCallProcessor
	 */
	BADCALL("org.binggo.apiwatchdog.processor.badcall.BadCallProcessor");
	
	private final String processorClassName;
	
	private ProcessorType(String processorClassName) {
		this.processorClassName = processorClassName;
	}
	
	public String getProcessorClassName() {
		return processorClassName;
	}

}
