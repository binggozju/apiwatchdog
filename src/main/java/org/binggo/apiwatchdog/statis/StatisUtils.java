package org.binggo.apiwatchdog.statis;

import java.util.Map;

import org.binggo.apiwatchdog.domain.ApiStatData;

import com.google.common.collect.Maps;

public class StatisUtils {
	
	// If a key in LoadingCache has not been accessed, it will be removed automatically.
	public static final long CACHE_EXPIRE_TIME = 6*60; // minutes
	
	// If the given key doesn't exist in LoadingCache, 
	// preload the statistical data from now to CACHE_PRE_LOAD hours later.
	public static final long CACHE_PRE_LOAD = 3*60*60*1000;  // milliseconds
	
	
	// division of response time
	public static final String INTERVAL_0_2 = "0-2";
	public static final String INTERVAL_2_5 = "2-5";
	public static final String INTERVAL_5_9 = "5-9";
	public static final String INTERVAL_9_MAX = "9-max";
	
	public static final String[] RESP_TIME_DIVISION = new String[] {
			INTERVAL_0_2,
			INTERVAL_2_5,
			INTERVAL_5_9,
			INTERVAL_9_MAX
	};
	
	public static Map<String, Integer> getRespTimeDistribution(ApiStatData apiStatData) {
		Map<String, Integer> distMap = Maps.newHashMap();
		
		distMap.put(INTERVAL_0_2, 
				apiStatData.getResptime0s1s() + apiStatData.getResptime1s2s());
		distMap.put(INTERVAL_2_5,
				apiStatData.getResptime2s3s() + apiStatData.getResptime3s4s() + apiStatData.getResptime4s5s());
		distMap.put(INTERVAL_5_9,
				apiStatData.getResptime5s6s() + apiStatData.getResptime6s7s() 
				+ apiStatData.getResptime7s8s() + apiStatData.getResptime8s9s());
		distMap.put(INTERVAL_9_MAX,
				apiStatData.getResptime9s10s() + apiStatData.getResptime10s11s() 
				+ apiStatData.getResptime11s12s() + apiStatData.getResptime12sMax());
		
		return distMap;
	}
	
	/**
	 * update mapCounter with the values in mapDelta
	 * @param mapCounter
	 * @param mapDelta
	 */
	public static void updateMapCounter(Map<String, Integer> mapCounter, Map<String, Integer> mapDelta) {
		for (Map.Entry<String, Integer> entry : mapDelta.entrySet()) {
			String intervalName = entry.getKey();
			Integer num = entry.getValue();
			
			if (!mapCounter.containsKey(intervalName)) {
				mapCounter.put(intervalName, 0);
			}
			mapCounter.put(intervalName, mapCounter.get(intervalName) + num);
		}	
	}

}
