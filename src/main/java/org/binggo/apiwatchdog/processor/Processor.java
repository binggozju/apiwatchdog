package org.binggo.apiwatchdog.processor;

import java.util.List;

import org.binggo.apiwatchdog.domain.ApiCall;

public interface Processor {
	
	/**
	 * Any initialization needed by the Processor.
	 */
	public void initialize();
	
	/**
	 * processing of a single ApiCall
	 * @param apiCall
	 */
	void process(ApiCall apiCall);
	
	/**
	 * processing of a batch of ApiCalls
	 * @param apiCallList
	 */
	void process(List<ApiCall> apiCallList);
	
	/**
	 * Perform any closing needed by the Processor.
	 */
	public void close();

}
