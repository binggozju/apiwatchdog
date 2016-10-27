package org.binggo.apiwatchdog;

import java.util.List;

/**
 * <p>A collector is connected to multiple {@link Processor}. 
 * It can collect {@link Event}, and dispatch them to the linked processors.<p>
 * @author Binggo
 */
public interface Collector {
	
	/**
	 * <p>collect a piece of message to the collector.</p>
	 * @param event message
	 */
	void collect(Event event);
	
	/**
	 * <p>collect a list of message to the collector.</p>
	 * @param eventList a list of events
	 */
	void collect(List<Event> eventList);
	
	/**
	 * <p>dispatch all the events of the collector to all the processors.<p>
	 */
	void dispatch();
}
