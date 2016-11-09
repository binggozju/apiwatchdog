package org.binggo.apiwatchdog.statis;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanUtils;

import org.binggo.apiwatchdog.mapper.ApiStatDataMapper;
import org.binggo.apiwatchdog.processor.analyzer.AnalyzerUtils;
import org.binggo.apiwatchdog.common.CommonUtils;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.domain.ApiStatData;

/**
 * <p>Statis can be used to provide the statistical data from Redis and MySQL.</p>
 * <p>You cann't modify the statistical data here.</p>
 * @author Binggo
 */
@Component
public class Statis {
	
	private static final Logger logger = LoggerFactory.getLogger(Statis.class);
	
	// data cache for statistical data in Redis
	// key: format: <api id>_<time slice string>; example: 1-20161109150500
	private LoadingCache<String, ApiStatData> dataCache;
	
	@Autowired
	public Statis(ApiStatDataMapper apiStatDataMapper, StringRedisTemplate template) {
		dataCache = CacheBuilder.newBuilder()
				.expireAfterAccess(StatisUtils.CACHE_EXPIRE_TIME, TimeUnit.MINUTES)
				.build(
					new StatisCacheLoader(apiStatDataMapper, template)
				);
	}
	
	////// provide the statistical data for a single API //////////
	/**
	 * get the time series of API call number 
	 * @param apiId API's id
	 * @param startTime format: 20161109181000
	 * @param endTime
	 * @return
	 */
	public Map<String, Integer> getApiCallNumTimeSeries(Integer apiId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Integer> result = Maps.newHashMap();
		
		Date startDate = null, endDate = null;
		try {
			startDate = CommonUtils.DATE_COMPACT_FORMAT.parse(startTime);
			endDate = CommonUtils.DATE_COMPACT_FORMAT.parse(endTime);
		} catch (ParseException e) {
			throw new WatchdogException(ReturnCode.INVALID_DATE);
		}
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			String redisKeyName = String.format("%d-%s", apiId, CommonUtils.DATE_COMPACT_FORMAT.format(currentDate));
			
			try {
				ApiStatData apiStatData = dataCache.get(redisKeyName);
				result.put(CommonUtils.DATE_COMPACT_FORMAT.format(currentDate), apiStatData.getCountTotal());
			} catch (ExecutionException ex) {
				result.put(CommonUtils.DATE_COMPACT_FORMAT.format(currentDate), 0);
			}		
		}
		
		return result;
	}
	
	public Map<String, Double> getApiAvailablityTimeSeries(Integer apiId, String startTime, String endTime) {
		Map<String, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<String, Double> getApiAccuracyTimeSeries(Integer apiId, String startTime, String endTime) {
		Map<String, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<String, Integer> getApiRespTimeDistributionTimeSeries(Integer apiId, String startTime, String endTime) {
		Map<String, Integer> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	
	////// provide the statistical data for an API provider (service) //////////
	public Map<String, Integer> getCallNumTimeSeries(Integer providerId, String startTime, String endTime) {
		Map<String, Integer> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<String, Double> getAvailablityTimeSeries(Integer providerId, String startTime, String endTime) {
		Map<String, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<String, Double> getAccuracyTimeSeries(Integer providerId, String startTime, String endTime) {
		Map<String, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<String, Integer> getRespTimeDistributionTimeSeries(Integer providerId, String startTime, String endTime) {
		Map<String, Integer> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<Integer, Double> getApiRankListByAvailablity(Integer providerId, String startTime, String endTime) {
		Map<Integer, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<Integer, Double> getApiRankListByAccuracy(Integer providerId, String startTime, String endTime) {
		Map<Integer, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	public Map<Integer, Double> getApiRankListByRespTime(Integer providerId, String startTime, String endTime) {
		Map<Integer, Double> result = Maps.newHashMap();
		// TODO
		return result;
	}
	
	/**
	 * load the statistical data into LoadingCache from Redis
	 */
	private class StatisCacheLoader extends CacheLoader<String, ApiStatData> {
		
		private ApiStatDataMapper apiStatDataMapper;
		
		private StringRedisTemplate template;
		
		public StatisCacheLoader(ApiStatDataMapper apiStatDataMapper, StringRedisTemplate template) {
			this.apiStatDataMapper = apiStatDataMapper;
			this.template = template;
		}

		@Override
		public ApiStatData load(String key) throws ExecutionException {
			ApiStatData returnData = null;
			
			// preparation work
			String[] strArray = ((String)key).split("-");
			int apiId = Integer.valueOf(strArray[0]);
			Date thisDate = null, preloadEndDate = null;  // start date and end date for preloading
			try {
				thisDate = CommonUtils.DATE_COMPACT_FORMAT.parse(strArray[1]);
			} catch (ParseException ex) {
				// nothing
			}
			
			// preload data from redis
			boolean success = true;  // whether or not succeed to preload all the needed data
			long timeSliceNum = StatisUtils.CACHE_PRE_LOAD/AnalyzerUtils.TIME_SLICE_LENGTH;
			for (long i = 0; i < timeSliceNum; i++) {
				Date currentDate = new Date(thisDate.getTime() + i * AnalyzerUtils.TIME_SLICE_LENGTH);
				String redisKeyName = String.format("%d-%s", apiId, CommonUtils.DATE_COMPACT_FORMAT.format(currentDate));
				
				ApiStatData apiStatData = new ApiStatData();
				apiStatData.setApiId(apiId);
				apiStatData.setStartTime(currentDate);
				
				Map<Object, Object> redisHash = template.boundHashOps(redisKeyName).entries();
				if (redisHash == null) {  // the hash doesn't exist in Redis, so preload it from mysql
					success = false;
					break;
				}
				
				Map<String, Integer> statDataMap = Maps.newHashMap();
				for (Map.Entry<Object, Object> entry : redisHash.entrySet()) {
					String entryKey = (String) entry.getKey();
					Integer entryValue = Integer.parseInt((String) entry.getValue());
					statDataMap.put(entryKey, entryValue);
				}
				try {
					BeanUtils.populate(apiStatData, statDataMap);
				} catch (IllegalAccessException | InvocationTargetException ex) {
					// nothing
				}
				
				dataCache.put(redisKeyName, apiStatData);
				if (i == 0) {
					returnData = apiStatData;
				}
			}
			if (success && returnData != null) {
				return returnData;
			}
			
			// preload data from mysql
			preloadEndDate = new Date(thisDate.getTime() + StatisUtils.CACHE_PRE_LOAD); // end date for preloading
			List<ApiStatData> dataList = apiStatDataMapper.getDataByTime(apiId, thisDate, preloadEndDate);
			if (dataList.size() != 0 && dataList.get(0).getStartTime().getTime() != thisDate.getTime()) {
				logger.error(String.format("the statistical data [%s] not found", key));
				return null;
			}
			returnData = dataList.get(0);
			
			for (ApiStatData data : dataList) {
				String dataRedisKey = String.format("%d-%s", 
						data.getApiId(), 
						CommonUtils.DATE_COMPACT_FORMAT.format(data.getStartTime())
					);
				dataCache.put(dataRedisKey, data);
			}
			
			return returnData;
		}
	}

}
