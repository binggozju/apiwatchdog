package org.binggo.apiwatchdog.collector;

public class CollectorConstants {
	
	public static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.collector.queue.capacity";
	public static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	
	public static final String QUEUE_TIMEOUT_CONFIG = "apiwatchdog.collector.queue.timeout";
	public static final Long QUEUE_TIMEOUT_DEFAULT =  2L; // 2 seconds for offer
	
	
	// if the collector queue is empty and collector thread is idle, make it sleep for a while
	public static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	public static final String COLLECTOR_NAME = "Simple-Collector";
	
}
