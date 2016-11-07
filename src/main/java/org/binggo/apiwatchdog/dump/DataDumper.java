package org.binggo.apiwatchdog.dump;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.IntegerConverter;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.binggo.apiwatchdog.TimerRunnable;
import org.binggo.apiwatchdog.WatchdogRunner;
import org.binggo.apiwatchdog.common.CommonUtils;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.domain.ApiStatData;
import org.binggo.apiwatchdog.mapper.ApiStatDataMapper;
import org.binggo.apiwatchdog.processor.analyzer.AnalyzerUtils;

/**
 * <p>Here are what DataDumper is aimed at:</p>
 * <p>1. Dump the statistical data from Redis to MySQL in order to save the overhead of memory
 * and provide more reliable storage for statistical data.</p>
 * <p>What's mroe, only after the DataDumper get the master role based on Zookeeper can it do the tasks above.</p>
 * @author Binggo
 */
@Component
public class DataDumper implements TimerRunnable {
	
	private static final Logger logger = LoggerFactory.getLogger(DataDumper.class);
	
	private Integer runPeriodLength;  // minutes
	
	private Boolean initialized = false;
	
	private Thread dataDumperThread;
	private DataDumperRunner dataDumperRunner;
	
	private String zkConnString;  // zookeeper connection string
	private CuratorFramework zkClient;
	private LeaderLatch leaderLatch;
	
	@Autowired
	private ApiStatDataMapper apiStatDataMapper;
	
	@Autowired
	private StringRedisTemplate template;
	
	static {
		// support to transform the hash in Redis to ApiStatData object
		ConvertUtils.register(new IntegerConverter(null), Integer.class);
	}
	
	@Autowired
	public DataDumper(WatchdogEnv env) {
		runPeriodLength = env.getInteger(DataDumperUtils.DATADUMPER_RUN_PERIOD_CONFIG,
				DataDumperUtils.DATADUMPER_RUN_PERIOD_DEFAULT);
		runPeriodLength = (runPeriodLength > DataDumperUtils.DATADUMPER_RUN_PERIOD_DEFAULT) ? 
				runPeriodLength : DataDumperUtils.DATADUMPER_RUN_PERIOD_DEFAULT;
		
		zkConnString = env.getString(DataDumperUtils.ZK_CONNECT_CONFIG, DataDumperUtils.ZK_CONNECT_DEFAULT);
		
		zkClient = CuratorFrameworkFactory.newClient(zkConnString, 
				DataDumperUtils.ZK_SESSION_TIMEOUT*1000,
				DataDumperUtils.ZK_CONNECTION_TIMEOUT*1000,
				new ExponentialBackoffRetry(DataDumperUtils.ZK_BASE_SLEEP_TIME*1000, DataDumperUtils.ZK_MAX_RETRY));
		
		leaderLatch = new LeaderLatch(zkClient, DataDumperUtils.ZK_LEADER_PATH);
	}
	
	private void initialize() {
		if (!initialized) {
			// add a listener to the leader latch
			LeaderLatchListener listener = new LeaderLatchListener() {
				@Override
				public void isLeader() {
					logger.info("get the leadership");
				}
				@Override
				public void notLeader() {
					logger.info("fail to get the leadership");
				}
			};
			leaderLatch.addListener(listener);
			
			dataDumperRunner = new DataDumperRunner();
			
			initialized = true;
		}
	}
	
	/**
	 * dump the statistical information from Redis to MySQL
	 */
	private void dump() {
		Date nowTime = new Date();
		Long currentTimeSliceIndex = nowTime.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		Long startTimeSliceIndex;
		
		String nextDumpTimeSlice = (String) template.opsForValue().get(DataDumperUtils.KEY_NEXT_DUMP_TIME_SLICE);
		if (nextDumpTimeSlice != null) {
			try {
				Date nextDumpDate = CommonUtils.DATE_COMPACT_FORMAT.parse(nextDumpTimeSlice);
				startTimeSliceIndex = nextDumpDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
			} catch (ParseException ex) {
				logger.error(String.format("The last dump time is invalid, quit to dump", nextDumpTimeSlice));
				return;
			}	
		} else {
			startTimeSliceIndex = (nowTime.getTime() - 2*runPeriodLength*60*1000)/AnalyzerUtils.TIME_SLICE_LENGTH;
		}
		
		for (long index = startTimeSliceIndex; index < currentTimeSliceIndex; index++) {
			Date sliceDate = new Date(index*AnalyzerUtils.TIME_SLICE_LENGTH);
			dump(CommonUtils.DATE_COMPACT_FORMAT.format(sliceDate));
		}
		
		// update the last dump time slice in redis
		Date dumpTime = new Date(currentTimeSliceIndex*AnalyzerUtils.TIME_SLICE_LENGTH);
		template.boundValueOps(DataDumperUtils.KEY_NEXT_DUMP_TIME_SLICE).set(CommonUtils.DATE_COMPACT_FORMAT.format(dumpTime));
	}
	
	/**
	 * dump all the keys whose names are matched to the given time slice
	 * @param timeSlice format: yyyyMMddHHmm00
	 */
	public void dump(String timeSlice) {
		String sliceKeysPattern = String.format("*-%s", timeSlice.trim());
		Set<String> sliceKeys = template.keys(sliceKeysPattern);
		
		for (String sliceKey : sliceKeys) {
			ApiStatData apiStatData = new ApiStatData();
			
			// get apiId and startTime
			String[] strArray = sliceKey.split("-");
			Integer apiId = Integer.valueOf(strArray[0]);
			apiStatData.setApiId(apiId);
			try {
				Date sliceStartTime = CommonUtils.DATE_COMPACT_FORMAT.parse(strArray[1]);
				apiStatData.setStartTime(sliceStartTime);
			} catch (ParseException e) {
				logger.error(String.format("Redis key [%s] is invalid", sliceKey));
				continue;
			}
			
			// get other fields
			Map<String, Integer> statDataMap = Maps.newHashMap();
			Map<Object, Object> sliceHash = template.boundHashOps(sliceKey).entries();
			for (Map.Entry<Object, Object> entry : sliceHash.entrySet()) {
				String entryKey = (String) entry.getKey();
				Integer entryValue = Integer.parseInt((String) entry.getValue());
				statDataMap.put(entryKey, entryValue);
			}
			
			try {
				BeanUtils.populate(apiStatData, statDataMap);
			} catch (IllegalAccessException | InvocationTargetException ex) {
				logger.error("fail to transform the hash to ApiStatData");
				continue;
			} 
			
			// dump the current slice
			try {
				apiStatDataMapper.insertSelective(apiStatData);
			} catch (Exception ex) {
				logger.warn("The API stat data to be inserted has already existed.");
			}
		}
	}

	@Scheduled(initialDelay=1000, fixedDelay=10000)
	@Override
	public void runTimerTask() {
		// create and start the data dumper thread
		if (dataDumperThread == null) {
			initialize();

			// start the leader latch
			zkClient.start();
			try {
				leaderLatch.start();
			} catch (Exception e) {
				CloseableUtils.closeQuietly(zkClient);
				CloseableUtils.closeQuietly(leaderLatch);
				logger.error("fail to start the leader latch");
				dataDumperRunner.setShouldStop(true);
				return;
			}
						
			// start the dumper thread
			dataDumperThread = new Thread(dataDumperRunner);
			dataDumperThread.setName(DataDumperUtils.DATA_DUMPER_NAME);
			
			logger.info(String.format("start the data dumper thread [%s]", dataDumperThread.getName()));
			dataDumperThread.start();
			return;
		}
		
		// check the data dumper thread
		if (!dataDumperRunner.shouldStop() && !dataDumperThread.isAlive()) {
			logger.warn(String.format("Thread [%s] is not alive, restart it", dataDumperThread.getName()));
			
			// create a new data dumper thread, and start it
			dataDumperThread = new Thread(dataDumperRunner);
			dataDumperThread.setName(DataDumperUtils.DATA_DUMPER_NAME);
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
			// wait the result of leader election for a while
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
			logger.info(String.format("Thread [%s] is running.", Thread.currentThread().getName()));
			
			while (!shouldStop()) {
				Date startDate = new Date();
				// only the leader can do the dump task
				if (leaderLatch.hasLeadership()) {
					logger.debug("I'm a leader now, start to dump");
					dump();
					logger.debug("complete the routine dump task");
				} else {
					logger.debug("I'm not the leader now");
				}
				Date endDate = new Date();
				
				long sleepTime = runPeriodLength*60*1000 - (endDate.getTime() - startDate.getTime());
				if (sleepTime <= 0) {
					logger.warn(String.format("The value of configuratioin [%s] is too small, increase it please", 
							DataDumperUtils.DATADUMPER_RUN_PERIOD_CONFIG));
					continue;
				}
				
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