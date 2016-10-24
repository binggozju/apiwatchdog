package org.binggo.apiwatchdog.collector;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.binggo.apiwatchdog.Processor;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.domain.ApiCall;

@Component
public class Collector {
	
	private static final Logger logger = LoggerFactory.getLogger(Collector.class);
	
	private static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.collector.queue.capacity";
	private static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	private static final String QUEUE_TIMEOUT_CONFIG = "apiwatchdog.collector.queue.timeout";
	private static final Long QUEUE_TIMEOUT_DEFAULT =  2L; // 2 seconds for offer
	
	private Integer queueCapacity;
	private Long queueTimeout;
	private LinkedBlockingQueue<ApiCall> collectorQueue;
	
	// if the collector queue is empty and collector thread is idle, make it sleep for a while
	private static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	private static final String COLLECTOR_THREAD_NAME = "Collector-Thread";
	private AtomicInteger collectorThreadId = new AtomicInteger(0);
	private Thread collectorThread;
	
	private Gson gson;
	
	@Autowired
	@Qualifier("facadeProcessor")
	private Processor processor;
	
	@Autowired
	public Collector(WatchdogEnv env) {
		queueCapacity = env.getInteger(QUEUE_CAPACITY_CONFIG, QUEUE_CAPACITY_DEFAULT);
		queueTimeout = env.getLong(QUEUE_TIMEOUT_CONFIG, QUEUE_TIMEOUT_DEFAULT);

		collectorQueue = new LinkedBlockingQueue<ApiCall>(queueCapacity);
		
		gson = new GsonBuilder().disableHtmlEscaping().create();
	}
	
	public void collect(ApiCall apiCall) throws WatchdogException {
		if (apiCall == null) {
			throw new WatchdogException(ReturnCode.INVALID_API_CALL);
		}
		
		try {
			Boolean success = collectorQueue.offer(apiCall, queueTimeout, TimeUnit.SECONDS);
			if (!success) {
				throw new WatchdogException(ReturnCode.COLLECT_QUEUE_FULL);
			}
		} catch (InterruptedException ex) {
			throw new WatchdogException(ReturnCode.INTERRUPTED, 
					"offering an ApiCall to collector queue has be interrupted.");
		}	
	}
	
	/**
	 * @param apiCallStr ApiCall in JSON format
	 * @throws WatchdogException
	 */
	public void collect(String apiCallStr) {
		try {
			ApiCall apiCall = gson.fromJson(apiCallStr, ApiCall.class);
			collect(apiCall);
			
		} catch (JsonSyntaxException ex) {
			logger.error(String.format("the api call of json format is invalid: %s", apiCallStr));
			return;
		} catch (WatchdogException ex) {
			logger.error("the collector queue is full, fail to put the ApiCall from kafka to collector");
			return;
		}
	}

	/**
	 * dispatch the API call to the processor
	 */
	@Scheduled(fixedDelay = 5*1000)
	public void dispatch() {
		// configure the collector thread
		collectorThread = Thread.currentThread();
		String threadName = String.format("%s-%d", COLLECTOR_THREAD_NAME, collectorThreadId.get());
		collectorThread.setName(threadName);
		collectorThreadId.getAndIncrement();
		logger.info(String.format("start collector thread [%s] to dispatch all the api call", threadName));
		
		// take ApiCall from the queue in loop, and process it by processor
		processor.initialize();
		
		while (true) {
			try {
				ApiCall apiCall = collectorQueue.poll();
				if (apiCall == null) {
					Thread.sleep(IDLE_SLEEP_TIME*1000);
					continue;
				}
				processor.process(apiCall);
				
			} catch (InterruptedException ex) {
				logger.error(String.format("the collector thread [%d] has been interrupted", threadName));
				// spring will restart the collector thread, there is no need to close the processor.
				//processor.close(); 
				return;
			}
		}
	}

	public Thread getCollectorThread() {
		return collectorThread;
	}
	
}
