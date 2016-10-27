package org.binggo.apiwatchdog;

import java.util.List;

/**
 * Processor can be used to process the events in various ways.
 * @author Binggo
 *
 */
public interface Processor {
	
	/**
	 * Any initialization needed by the Processor.
	 */
	public void initialize();
	
	/**
	 * take a single event
	 * @param event
	 */
	void take(Event event);
	
	/**
	 * take a batch of events
	 * @param eventList
	 */
	void take(List<Event> eventList);
	
	/**
	 * Perform any closing needed by the Processor.
	 */
	public void close();

	/**
	 * process all the events stored in the processor.
	 */
	void process();
}
