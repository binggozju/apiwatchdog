package org.binggo.apiwatchdog;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public abstract class WatchdogProcessor implements Processor, TimerRunnable {
	
	private static final Logger logger = LoggerFactory.getLogger(WatchdogProcessor.class);
	
	private String name;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	
	protected LinkedBlockingQueue<Event> processQueue;
	protected Map<Thread, ProcessorRunner> runnerMap;
	
	
	public WatchdogProcessor(String name) {
		this.name = name;
		this.runnerMap = Maps.newHashMap();
	}
	
	@Override
	public void initialize() {
		setInitialized(true);
		logger.info(String.format("%s has been initialized.", getName()));
	}
	
	@Override
	public void take(Event event) {
		if (!isPermitted(event)) {
			return;
		}
		
		// must not block here
		if (!processQueue.offer(event)) {
			logger.error(String.format("fail to offer the event [%s] to the queue of %s", 
					event.toString(), getName()));
		}
	}
	
	@Override
	public void take(List<Event> eventList) {
		for (Event event : eventList) {
			take(event);
		}
	}
	
	@Override
	public void close() {
		logger.info(String.format("%s has been closed.", getName()));
	}
	
	/**
	 * make a judge about whether or not a given event can be permitted to the processor's queue.
	 * @param event
	 * @return
	 */
	protected abstract boolean isPermitted(Event event);
	
	public void stopAllRunners() {
		if (runnerMap == null) {
			return;
		}
		
		for (Map.Entry<Thread, ProcessorRunner> entry :runnerMap.entrySet()) {
			ProcessorRunner processorRunner = entry.getValue();
			Thread processThread = entry.getKey();
			
			processorRunner.setShouldStop(true);
			processThread.interrupt();
			
			while (processThread.isAlive()) {
				try {
					processThread.join(500);
				} catch (InterruptedException ex) {
					logger.info("Interrupted while waiting for runner thread to exit. Exception follows.", ex);
				}
			}
		}
	}
	
	protected Boolean isInitialized() {
		return initialized.get();
	}

	protected void setInitialized(Boolean b) {
		initialized.set(b);
	}
	
	/**
	 * <p>check all the processor threads in runnerMap.</p>
	 * <p>clear the terminated threads, and restart them with new threads.</p>
	 */
	protected void checkProcessorHealth() {
		Map<Thread, ProcessorRunner> cacheRunnerMap = Maps.newHashMap();
		
		Iterator<Entry<Thread, ProcessorRunner>> it = runnerMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Thread, ProcessorRunner> entry = it.next();
			ProcessorRunner processorRunner = entry.getValue();
			Thread processorThread = entry.getKey();
			
			if (!processorRunner.shouldStop() && !processorThread.isAlive()) {
				logger.warn(String.format("Thread [%s] is not alive, restart it", processorThread.getName()));
				
				// create a new processor thread, and start it
				Thread newProcessorThread = new Thread(processorRunner);
				newProcessorThread.setName(processorThread.getName());
				cacheRunnerMap.put(newProcessorThread, processorRunner);
				newProcessorThread.start();
				
				it.remove();
			}
		}
		runnerMap.putAll(cacheRunnerMap);
	}
	
	
	public String getName() {
		return name;
	}
	
	public static class ProcessorRunner extends WatchdogRunner {
		
		private static final Logger logger = LoggerFactory.getLogger(ProcessorRunner.class);
		
		private WatchdogProcessor processor;
		
		public ProcessorRunner(WatchdogProcessor processor) {
			this.processor = processor;
		}

		@Override
		public void run() {
			logger.info(String.format("Thread [%s] is running.", Thread.currentThread().getName()));
			
			while (!shouldStop()) {
				processor.process();
			}
			
			logger.info(String.format("Thread [%s] has been stoped.", Thread.currentThread().getName()));
			
		}
		
	}
}
