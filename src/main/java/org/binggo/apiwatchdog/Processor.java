package org.binggo.apiwatchdog;

import java.util.List;

import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * Processor can be used to consume the API call information in various ways.
 * @author Administrator
 *
 */
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
