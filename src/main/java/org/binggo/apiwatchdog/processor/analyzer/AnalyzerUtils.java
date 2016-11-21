package org.binggo.apiwatchdog.processor.analyzer;

import java.util.Date;

import org.binggo.apiwatchdog.common.CommonUtils;

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
	public static final String KEY_COUNT_TOTAL = "countTotal";
	public static final String KEY_COUNT_TIMEOUT = "countTimeout";
	public static final String KEY_COUNT_NOT200 = "countNot200";
	public static final String KEY_200_NOT0 = "count200Not0";
	public static final String KEY_RESPTIME_TOTAL = "resptimeTotal";
	public static final String KEY_RESPTIME_0S_1S = "resptime0s1s";
	public static final String KEY_RESPTIME_1S_2S = "resptime1s2s";
	public static final String KEY_RESPTIME_2S_3S = "resptime2s3s";
	public static final String KEY_RESPTIME_3S_4S = "resptime3s4s";
	public static final String KEY_RESPTIME_4S_5S = "resptime4s5s";
	public static final String KEY_RESPTIME_5S_6S = "resptime5s6s";
	public static final String KEY_RESPTIME_6S_7S = "resptime6s7s";
	public static final String KEY_RESPTIME_7S_8S = "resptime7s8s";
	public static final String KEY_RESPTIME_8S_9S = "resptime8s9s";
	public static final String KEY_RESPTIME_9S_10S = "resptime9s10s";
	public static final String KEY_RESPTIME_10S_11S = "resptime10s11s";
	public static final String KEY_RESPTIME_11S_12S = "resptime11s12s";
	public static final String KEY_RESPTIME_12S_MAX = "resptime12sMax";
	
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
	public static final Integer TIME_SLICE_LENGTH = 5*60*1000; // milliseconds
	
	public static String getTimeSlice(Date date) {
		Long longStartOfSlice = (date.getTime()/TIME_SLICE_LENGTH) * TIME_SLICE_LENGTH;
		Date dateStartOfSlice = new Date(longStartOfSlice);
		
		return CommonUtils.getCompactDateFormat().format(dateStartOfSlice);
	}
	
	/**
	 * <p>get the key name of the time slice (5 minute) in redis corresponding to the given date.</p>
	 * <p>the format of redis key: apiid_yyyyMMddHHmm00</p>
	 * @param date
	 * @return
	 */
	public static String getRedisKeyName(Integer apiId, Date date) {
		String redisKeyName = String.format("%d-%s", apiId, getTimeSlice(date));	
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
