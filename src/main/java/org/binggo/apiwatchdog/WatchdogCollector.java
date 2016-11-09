package org.binggo.apiwatchdog;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WatchdogCollector implements Collector, TimerRunnable {
	private String name;
	
	protected LinkedBlockingQueue<Event> collectQueue;
	
	protected Processor processor;
	
	protected WatchdogCollector(String name) {
		this.name = name;
	}
	
	@Override
	public void collect(List<Event> eventList) {
		for (Event event : eventList) {
			collect(event);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public static class CollectorRunner extends WatchdogRunner {
		
		private static final Logger logger = LoggerFactory.getLogger(CollectorRunner.class);
		
		private WatchdogCollector collector;
		
		public CollectorRunner(WatchdogCollector collector) {
			this.collector = collector;
		}

		@Override
		public void run() {
			logger.info(String.format("Thread [%s] is running, start to dispatch events.", 
					Thread.currentThread().getName()));
			
			while (!shouldStop()) {
				collector.dispatch();
			}
			
			logger.info(String.format("Thread [%s] has been stoped.", Thread.currentThread().getName()));
		}
		
	}
}
