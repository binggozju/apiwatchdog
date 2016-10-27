package org.binggo.apiwatchdog.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.WatchdogCollector;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;

@Component("simpleCollector")
public class SimpleCollector extends WatchdogCollector {
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleCollector.class);
	
	private Integer capacity;
	private Long timeout;
	
	private CollectorRunner collectorRunner;
	private Thread collectorThread;
	
	@Autowired
	public SimpleCollector(WatchdogEnv env, @Qualifier("facadeProcessor") Processor processor) {
		super(CollectorConstants.COLLECTOR_NAME);
		// get configuration
		capacity = env.getInteger(CollectorConstants.QUEUE_CAPACITY_CONFIG, CollectorConstants.QUEUE_CAPACITY_DEFAULT);
		timeout = env.getLong(CollectorConstants.QUEUE_TIMEOUT_CONFIG, CollectorConstants.QUEUE_TIMEOUT_DEFAULT);
		
		this.collectQueue = new LinkedBlockingQueue<Event>(capacity);
		this.processor = processor;
	}

	@Override
	public void collect(Event event) throws WatchdogException {
		if (event == null || event.getBody() == null) {
			throw new WatchdogException(ReturnCode.INVALID_EVENT);
		}
		
		try {
			Boolean success = collectQueue.offer(event, timeout, TimeUnit.SECONDS);
			if (!success) {
				throw new WatchdogException(ReturnCode.COLLECT_QUEUE_FULL);
			}
		} catch (InterruptedException ex) {
			throw new WatchdogException(ReturnCode.INTERRUPTED, 
					"offering an event to collect queue has be interrupted.");
		}		
	}

	/**
	 * dispatch all events in the collector to all linked processors.
	 */
	@Override
	public void dispatch() {
		Event event = collectQueue.poll();
		if (event == null) {
			try {
				Thread.sleep(CollectorConstants.IDLE_SLEEP_TIME*1000);
			} catch (InterruptedException ex) {
				//ex.printStackTrace();
			}
			return;
		}
		processor.take(event);
	}
	
	@Scheduled(initialDelay=500, fixedDelay = 5000)
	@Override
	public void runTimerTask() {
		if (collectorRunner == null) {
			processor.initialize();
			
			collectorRunner = new CollectorRunner(this);
			collectorThread = new Thread(collectorRunner);
			collectorThread.setName(CollectorConstants.COLLECTOR_NAME);
			
			logger.info(String.format("start the collector thread [%s]", collectorThread.getName()));
			collectorThread.start();
			
			return;
		}
		
		if (!collectorRunner.shouldStop() && !collectorThread.isAlive()) {
			logger.warn(String.format("Thread [%s] is not alive, restart it", collectorThread.getName()));
			collectorThread.start();	
		} 
	}
	
	public void stopCollectorRunner() {
		if (collectorThread != null) {
			collectorRunner.setShouldStop(true);
			collectorThread.interrupt();
			
			while (collectorThread.isAlive()) {
				try {
					collectorThread.join(500);
				} catch (InterruptedException ex) {
					logger.info("Interrupted while waiting for runner thread to exit. Exception follows.", ex);
				}
			}
		}
	}
	
}