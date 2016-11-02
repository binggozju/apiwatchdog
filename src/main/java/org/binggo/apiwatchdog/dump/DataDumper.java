package org.binggo.apiwatchdog.dump;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.binggo.apiwatchdog.TimerRunnable;
import org.binggo.apiwatchdog.WatchdogRunner;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.mapper.ApiStatDataMapper;

/**
 * <p>Here are what DataDumper is aimed at:</p>
 * <p>1. Dump the statistical data from Redis to MySQL in order to save the overhead of memory
 * and provide more reliable storage for statistical data.</p>
 * <p>2. Clear the old keys beyond their expire time in Redis periodically.</p>
 * <p>What's mroe, only after the DataDumper get the master role based on Zookeeper can it do the tasks above.</p>
 * @author Binggo
 */
@Component
public class DataDumper implements TimerRunnable {
	
	private static final Logger logger = LoggerFactory.getLogger(DataDumper.class);
	
	private static final String REDIS_KEYS_EXPIRE_CONFIG = "apiwatchdog.redis.keys.expire";
	private static final Integer REDIS_KEYS_EXPIRE_DEFAULT = 7*24*60; // minutes
	private static final String DATA_DUMPER_NAME = "Data-Dumper";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private Integer keysExpireTime; // minutes
	
	private Boolean initialized = false;
	
	private Thread dataDumperThread;
	private DataDumperRunner dataDumperRunner;
	
	@Autowired
	private ApiStatDataMapper apiStatDataMapper;
	
	@Autowired
	private StringRedisTemplate template;
	
	@Autowired
	private ZKHelper zkHelper;
	
	@Autowired
	public DataDumper(WatchdogEnv env) {
		keysExpireTime = env.getInteger(REDIS_KEYS_EXPIRE_CONFIG, REDIS_KEYS_EXPIRE_DEFAULT);
	}
	
	private void initialize() {
		if (!initialized) {
			dataDumperRunner = new DataDumperRunner();
			
			initialized = true;
		}
	}
	
	private void clearExpiredKeys() {
		// TOOD
	}
	
	private void dump() {
		// TODO
	}
	

	@Scheduled(initialDelay=5000, fixedDelay=5000)
	@Override
	public void runTimerTask() {
		// create and start the data dumper thread
		if (dataDumperThread == null) {
			initialize();
			
			dataDumperThread = new Thread(dataDumperRunner);
			dataDumperThread.setName(DATA_DUMPER_NAME);
			
			logger.info(String.format("start the data dumper thread [%s]", dataDumperThread.getName()));
			dataDumperThread.start();
			return;
		}
		
		// check the data dumper thread
		if (!dataDumperRunner.shouldStop() && !dataDumperThread.isAlive()) {
			logger.warn(String.format("Thread [%s] is not alive, restart it", dataDumperThread.getName()));
			
			// create a new data dumper thread, and start it
			dataDumperThread = new Thread(dataDumperRunner);
			dataDumperThread.setName(DATA_DUMPER_NAME);
			dataDumperThread.start();
		}
	}
	
	public void stopDataDumper() {
		if (dataDumperRunner == null) {
			return;
		}
		dataDumperRunner.setShouldStop(true);
		
		if (dataDumperThread == null) {
			return;
		}
		dataDumperThread.interrupt();
		while (dataDumperThread.isAlive()) {
			try {
				dataDumperThread.join(500);
			} catch (InterruptedException ex) {
				logger.info("Interrupted while waiting for data dumper thread to exit. Exception follows.", ex);
			}
		}
	}
	
	private class DataDumperRunner extends WatchdogRunner {
		
		public DataDumperRunner() {}

		@Override
		public void run() {
			logger.info(String.format("Thread [%s] is running.", Thread.currentThread().getName()));
			
			while (!shouldStop()) {
				Date startDate = new Date();
				
				// TODO
				
				Date endDate = new Date();
				logger.info(String.format("complete the rounite data dumping (start time: %s; end time: %s)", 
						DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate)));
				
				long sleepTime = endDate.getTime() - startDate.getTime(); // TODO: modify
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException ex) {
					//ex.printStackTrace();
					logger.warn("Thread [%s] has been interrupted while sleeping. Exiting.", 
							Thread.currentThread().getName());
				}
			}
			
			logger.info(String.format("Thread [%s] has been stoped.", Thread.currentThread().getName()));
		}
	}

}
