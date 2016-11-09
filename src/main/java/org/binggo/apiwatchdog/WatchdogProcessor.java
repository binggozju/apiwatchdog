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
	
	// if the queue is empty and process thread is idle, make it sleep for a while
	private static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	private String name;	// the name of processor [thread]
	
	protected Integer capacity;	// the capacity of the processor's queue
	protected Integer processorNum;	// the number of processor threads
	
	private LinkedBlockingQueue<Event> processQueue;
	private Map<Thread, ProcessorRunner> runnerMap;
	
	private AtomicBoolean initialized = new AtomicBoolean(false);
	
	protected WatchdogProcessor(String name) {
		this.name = name;
		this.runnerMap = Maps.newHashMap();
	}
	
	protected abstract void doInitialize();
	
	@Override
	public void initialize() {
		if (initialized.get()) {
			return;
		}
		
		processQueue = new LinkedBlockingQueue<Event>(capacity);
		doInitialize();
		
		setInitialized(true);
		logger.info(String.format("%s has been initialized.", getName()));
	}
	
	/**
	 * make a judge about whether or not a given event can be permitted to the processor's queue.
	 * @param event
	 * @return
	 */
	protected abstract boolean isPermitted(Event event);
	
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
	 * process the event in the way of current processor.
	 * @param event
	 */
	protected abstract void processEvent(Event event);
	
	@Override
	public void process() {
		Event event = processQueue.poll();
		if (event == null) {
			try {
				Thread.sleep(IDLE_SLEEP_TIME*1000);
			} catch (InterruptedException ex) {
				//ex.printStackTrace();
				logger.warn("Thread [%s] has been interrupted while processing events. Exiting.", 
						Thread.currentThread().getName());
			}
			return;
		}
		
		processEvent(event);
	}
	
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
	
	protected boolean isInitialized() {
		return initialized.get();
	}

	private void setInitialized(Boolean b) {
		initialized.set(b);
	}
	
	/**
	 * create the processor runners and start them.
	 * if the processor runner has already been created, check whether they are alive.
	 */
	protected void createAndCheckProcessors() {
		if (runnerMap.size() == 0) {
			ProcessorRunner processorRunner = new ProcessorRunner(this);
			
			for (int i = 0; i < processorNum; i++) {
				Thread processorThread = new Thread(processorRunner);
				
				processorThread.setName(String.format("%s-%d", getName(), i));
				
				runnerMap.put(processorThread, processorRunner);
				logger.info(String.format("start the processor thread [%s]", processorThread.getName()));
				processorThread.start();
			}
			return;
		}
		
		checkProcessorHealth();
	}
	
	/**
	 * <p>check all the processor threads in runnerMap.</p>
	 * <p>clear the terminated threads, and restart them with new threads.</p>
	 */
	private void checkProcessorHealth() {
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
