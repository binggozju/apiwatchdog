package org.binggo.apiwatchdog.processor.analyzer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnalyzerUtils {
	// configuration name and its default value
	public static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.anaylzer.queue.capacity";
	public static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	
	public static final String ANALYZER_THREAD_NUM_CONFIG = "apiwatchdog.analyzer.runner.num";
	public static final Integer ANALYZER_THREAD_NUM_DEFAULT = 3;
	
	public static final String REDIS_KEYS_EXPIRE_CONFIG = "apiwatchdog.redis.keys.expire";
	public static final Integer REDIS_KEYS_EXPIRE_DEFAULT = 7*24*60; // minutes
	
	public static final String PROCESSOR_NAME = "Analyzer-Processor";
	
	
	// keys's name in redis
	public static final String KEY_COUNT_TOTAL = "count_total";
	public static final String KEY_COUNT_TIMEOUT = "count_timeout";
	public static final String KEY_COUNT_NOT200 = "count_not200";
	public static final String KEY_200_NOT0 = "count_200_not0";
	public static final String KEY_RESPTIME_TOTAL = "resptime_total";
	public static final String KEY_RESPTIME_0S_1S = "resptime_0s_1s";
	public static final String KEY_RESPTIME_1S_2S = "resptime_1s_2s";
	public static final String KEY_RESPTIME_2S_3S = "resptime_2s_3s";
	public static final String KEY_RESPTIME_3S_4S = "resptime_3s_4s";
	public static final String KEY_RESPTIME_4S_5S = "resptime_4s_5s";
	public static final String KEY_RESPTIME_5S_6S = "resptime_5s_6s";
	public static final String KEY_RESPTIME_6S_7S = "resptime_6s_7s";
	public static final String KEY_RESPTIME_7S_8S = "resptime_7s_8s";
	public static final String KEY_RESPTIME_8S_9S = "resptime_8s_9s";
	public static final String KEY_RESPTIME_9S_10S = "resptime_9s_10s";
	public static final String KEY_RESPTIME_10S_11S = "resptime_10s_11s";
	public static final String KEY_RESPTIME_11S_12S = "resptime_11s_12s";
	public static final String KEY_RESPTIME_12S_MAX = "resptime_12s_max";
	
	public static final String[] RESPTIME_KEYS = new String[] {
			KEY_RESPTIME_0S_1S,
			KEY_RESPTIME_1S_2S,
			KEY_RESPTIME_2S_3S,
			KEY_RESPTIME_3S_4S,
			KEY_RESPTIME_4S_5S,
			KEY_RESPTIME_5S_6S,
			KEY_RESPTIME_6S_7S,
			KEY_RESPTIME_7S_8S,
			KEY_RESPTIME_8S_9S,
			KEY_RESPTIME_9S_10S,
			KEY_RESPTIME_10S_11S,
			KEY_RESPTIME_11S_12S,
			KEY_RESPTIME_12S_MAX
		};
	
	
	// length of the time slice for analysis and statistics
	private static final Integer TIME_SLICE_LENGTH = 5*60*1000; // 5 minutes
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * get the key name of the time slice (5 minute) in redis corresponding to the given date
	 * @param date
	 * @return
	 */
	public static String getRedisKeyName(Integer apiId, Date date) {
		Long longStartOfSlice = (date.getTime()/TIME_SLICE_LENGTH) * TIME_SLICE_LENGTH;
		Date dateStartOfSlice = new Date(longStartOfSlice);
		
		String redisKeyName = String.format("%d-%s", apiId, DATE_FORMAT.format(dateStartOfSlice));	
		return redisKeyName;
	}
	
	public static String getRespTimeKey(int respTime) {
		if (respTime < RESPTIME_KEYS.length - 1) {
			return RESPTIME_KEYS[respTime];
		} else {
			return RESPTIME_KEYS[RESPTIME_KEYS.length - 1];
		}
	}

}
