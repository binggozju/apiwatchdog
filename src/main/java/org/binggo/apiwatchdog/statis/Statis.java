package org.binggo.apiwatchdog.statis;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
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
import org.binggo.apiwatchdog.mapper.ApiItemMapper;
import org.binggo.apiwatchdog.mapper.ApiStatDataMapper;
import org.binggo.apiwatchdog.processor.analyzer.AnalyzerUtils;
import org.binggo.apiwatchdog.common.CommonUtils;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.domain.ApiItem;
import org.binggo.apiwatchdog.domain.ApiStatData;

/**
 * <p>Statis can be used to provide the statistical data from Redis and MySQL.</p>
 * <p>You cann't modify the statistical data here.</p>
 * @author Binggo
 */
@Component
public class Statis {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Statis.class);
	
	// data cache for statistical data in Redis
	// key: format: <api id>_<time slice string>; example: 1-20161109150500
	private LoadingCache<String, ApiStatData> dataCache;
	
	@Autowired
	private ApiItemMapper apiItemMapper;
	
	@Autowired
	public Statis(ApiStatDataMapper apiStatDataMapper, StringRedisTemplate template) {
		dataCache = CacheBuilder.newBuilder()
				.expireAfterAccess(StatisUtils.CACHE_EXPIRE_TIME, TimeUnit.MINUTES)
				.build(
					new StatisCacheLoader(apiStatDataMapper, template)
				);
	}
	
	private Date getValidDate(String dateStr) throws WatchdogException {
		if (dateStr == null) {
			throw new WatchdogException(ReturnCode.INVALID_DATE);
		}
		try {
			DateFormat dateFormat = CommonUtils.getCompactDateFormat();
			Date resultDate = dateFormat.parse(dateStr);
			
			Date nowDate = new Date();
			if (resultDate.getTime() > nowDate.getTime()) {
				resultDate = nowDate;
			}
			return resultDate;
			
		} catch (ParseException e) {
			throw new WatchdogException(ReturnCode.INVALID_DATE);
		}
	}
	
	//////////// provide the statistical data for a single API ////////////////
	/**
	 * get the time series of API call number 
	 * @param apiId API's id
	 * @param startTime format: 20161109181000
	 * @param endTime
	 * @return
	 */
	public Map<String, Integer> getApiCallNumTimeSeries(Integer apiId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Integer> result = Maps.newTreeMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			String redisKeyName = String.format("%d-%s", apiId, compactFormat.format(currentDate));
			
			try {
				ApiStatData apiStatData = dataCache.get(redisKeyName);
				result.put(compactFormat.format(currentDate), apiStatData.getCountTotal());
			} catch (ExecutionException ex) {
				// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				result.put(compactFormat.format(currentDate), 0);
			}
		}
		return result;
	}
	
	/**
	 * availablity = (countTotal - countTimeout)/countTotal
	 * @param apiId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String, Double> getApiAvailabilityTimeSeries(Integer apiId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Double> result = Maps.newTreeMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			String redisKeyName = String.format("%d-%s", apiId, compactFormat.format(currentDate));
			
			try {
				ApiStatData apiStatData = dataCache.get(redisKeyName);
				if (apiStatData.getCountTotal() == 0) {
					result.put(compactFormat.format(currentDate), 1.00);
					continue;
				}
				double availablity = (apiStatData.getCountTotal() - apiStatData.getCountTimeout()) * 1.00 / apiStatData.getCountTotal();
				result.put(compactFormat.format(currentDate), availablity);
			} catch (ExecutionException ex) {
				// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				result.put(compactFormat.format(currentDate), 1.00);
			}
		}
		return result;
	}
	
	/**
	 * accuracy = (countTotal - countTimeout - countNot200 - count200Not0)/countTotal
	 * @param apiId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String, Double> getApiAccuracyTimeSeries(Integer apiId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Double> result = Maps.newTreeMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			String redisKeyName = String.format("%d-%s", apiId, compactFormat.format(currentDate));
			
			try {
				ApiStatData apiStatData = dataCache.get(redisKeyName);
				if (apiStatData.getCountTotal() == 0) {
					result.put(compactFormat.format(currentDate), 1.00);
					continue;
				}
				double accuracy = (apiStatData.getCountTotal() - apiStatData.getCountTimeout() - apiStatData.getCountNot200() 
						- apiStatData.getCount200Not0()) * 1.00 / apiStatData.getCountTotal();
				result.put(compactFormat.format(currentDate), accuracy);
			} catch (ExecutionException ex) {
				// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				result.put(compactFormat.format(currentDate), 1.00);
			}
		}
		return result;
	}
	
	public Map<String, Double> getApiAvgResptimeTimeSeries(Integer apiId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Double> result = Maps.newTreeMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			String redisKeyName = String.format("%d-%s", apiId, compactFormat.format(currentDate));
			try {
				ApiStatData apiStatData = dataCache.get(redisKeyName);
				int resptimeTotal = apiStatData.getResptimeTotal();
				int callNumWithResp = apiStatData.getCountTotal() - apiStatData.getCountTimeout();
				if (callNumWithResp == 0) {
					result.put(compactFormat.format(currentDate), 0.00);
					continue;
				}
				double avgResptime = resptimeTotal * 1.00 / callNumWithResp;
				result.put(compactFormat.format(currentDate), avgResptime);
			} catch (ExecutionException ex) {
				// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				result.put(compactFormat.format(currentDate), 0.00);
			}
		}
		return result;
	}
	
	/**
	 * division for response time: 0-2, 2-5, 5-9, 9+
	 * @param apiId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public Map<String, Integer> getApiResptimeDistribution(Integer apiId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Integer> result = Maps.newTreeMap();
		for (String intervalName : StatisUtils.RESP_TIME_DIVISION) {
			result.put(intervalName, 0);
		}
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			String redisKeyName = String.format("%d-%s", apiId, compactFormat.format(currentDate));
			
			try {
				ApiStatData apiStatData = dataCache.get(redisKeyName);
				StatisUtils.updateMapCounter(result, StatisUtils.getRespTimeDistribution(apiStatData));
			} catch (ExecutionException ex) {
				// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				// nothing to do
			}		
		}
		return result;
	}
	
	
	////// provide the statistical data for an API provider (service) //////////
	public Map<String, Integer> getCallNumTimeSeries(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Integer> result = Maps.newTreeMap();
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			int callNum = 0;
			
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			for (ApiItem api : apiItemList) {
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					callNum += apiStatData.getCountTotal();
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
					// nothing to do
				}	
			} // end of loop for apiItemList
			
			result.put(compactFormat.format(currentDate), callNum);
		}
		return result;
	}
	
	public Map<String, Double> getAvailabilityTimeSeries(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Double> result = Maps.newTreeMap();
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			int countTotal = 0, countTimeout = 0;
			
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			for (ApiItem api : apiItemList) {
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					countTotal += apiStatData.getCountTotal();
					countTimeout += apiStatData.getCountTimeout();
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
					// nothing to do
				}
			}
			
			if (countTotal == 0) {
				result.put(compactFormat.format(currentDate), 1.00);
				continue;
			}
			double availablity = (countTotal - countTimeout) * 1.00 / countTotal;
			result.put(compactFormat.format(currentDate), availablity);
		}	
		return result;
	}
	
	public Map<String, Double> getAccuracyTimeSeries(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Double> result = Maps.newTreeMap();
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			int countTotal = 0, countTimeout = 0, countNot200 = 0, count200Not0 = 0;
			
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			for (ApiItem api : apiItemList) {
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					countTotal += apiStatData.getCountTotal();
					countTimeout += apiStatData.getCountTimeout();
					countNot200 += apiStatData.getCountNot200();
					count200Not0 += apiStatData.getCount200Not0();
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
					// nothing to do
				}
			}
			
			if (countTotal == 0) {
				result.put(compactFormat.format(currentDate), 1.00);
				continue;
			}
			double accuracy = (countTotal - countTimeout - countNot200 - count200Not0) * 1.00 / countTotal;
			result.put(compactFormat.format(currentDate), accuracy);
		}	
		return result;
	}
	
	public Map<String, Double> getAvgResptimeTimeSeries(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Double> result = Maps.newTreeMap();
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			int resptimeTotal = 0, callNumWithResp = 0;
			
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			for (ApiItem api : apiItemList) {
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					resptimeTotal += apiStatData.getResptimeTotal();
					callNumWithResp += apiStatData.getCountTotal() - apiStatData.getCountTimeout();	
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
					// nothing
				}
			}

			if (callNumWithResp == 0) {
				result.put(compactFormat.format(currentDate), 0.00);
				continue;
			}
			double avgResptime = resptimeTotal * 1.00 / callNumWithResp;
			result.put(compactFormat.format(currentDate), avgResptime);
		}
		return result;
	}
	
	
	public Map<String, Integer> getResptimeDistribution(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<String, Integer> result = Maps.newTreeMap();
		for (String intervalName : StatisUtils.RESP_TIME_DIVISION) {
			result.put(intervalName, 0);
		}
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
			Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
			for (ApiItem api : apiItemList) {
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					StatisUtils.updateMapCounter(result, StatisUtils.getRespTimeDistribution(apiStatData));
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
					// nothing to do
				}
			}	
		}
		return result;
	}
	
	
	///////////////// get API rank list  ///////////////////////////
	/**
	 * @param providerId
	 * @param startTime
	 * @param endTime
	 * @return Map's key is an API id， and its value is the availability of this API.
	 */
	public Map<Integer, Double> getApiRankListByAvailablity(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<Integer, Double> result = Maps.newHashMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		for (ApiItem api : apiItemList) {
			int countTotal = 0, countTimeout = 0;
			
			for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
				Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					countTotal += apiStatData.getCountTotal();
					countTimeout += apiStatData.getCountTimeout();
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				}
			}

			if (countTotal == 0) {
				result.put(api.getApiId(), 1.00);
				continue;
			}
			double availability = (countTotal - countTimeout) * 1.00 / countTotal;
			result.put(api.getApiId(), availability);
		}
		return result;
	}
	
	public Map<Integer, Double> getApiRankListByAccuracy(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<Integer, Double> result = Maps.newHashMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		for (ApiItem api : apiItemList) {
			int countTotal = 0, countTimeout = 0, countNot200 = 0, count200Not0 = 0;
			
			for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
				Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					countTotal += apiStatData.getCountTotal();
					countTimeout += apiStatData.getCountTimeout();
					countNot200 += apiStatData.getCountNot200();
					count200Not0 += apiStatData.getCount200Not0();
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				}
			}
			
			if (countTotal == 0) {
				result.put(api.getApiId(), 1.00);
				continue;
			}
			double accuracy = (countTotal - countTimeout - countNot200 - count200Not0) * 1.00 / countTotal;
			result.put(api.getApiId(), accuracy);	
		}
		return result;
	}
	
	public Map<Integer, Double> getApiRankListByAvgResptime(Integer providerId, String startTime, String endTime) 
			throws WatchdogException {
		Map<Integer, Double> result = Maps.newHashMap();
		
		DateFormat compactFormat = CommonUtils.getCompactDateFormat();
		Date startDate = getValidDate(startTime);
		Date endDate = getValidDate(endTime);
		long startTimeSliceIndex = startDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		long endTimeSliceIndex = endDate.getTime()/AnalyzerUtils.TIME_SLICE_LENGTH;
		
		List<ApiItem> apiItemList = apiItemMapper.listApiItemsByProviderId(providerId);
		for (ApiItem api : apiItemList) {
			int resptimeTotal = 0, callNumWithResp = 0;
			
			for (long i = startTimeSliceIndex; i < endTimeSliceIndex; i++) {
				Date currentDate = new Date(i * AnalyzerUtils.TIME_SLICE_LENGTH);
				String redisKeyName = String.format("%d-%s", api.getApiId(), compactFormat.format(currentDate));
				try {
					ApiStatData apiStatData = dataCache.get(redisKeyName);
					resptimeTotal += apiStatData.getResptimeTotal();
					callNumWithResp += apiStatData.getCountTotal() - apiStatData.getCountTimeout();
				} catch (ExecutionException ex) {
					// the meaning of exception above: the statistical data doesn't exist in both Redis and MySQL.
				}
			}
			
			if (callNumWithResp == 0) {
				result.put(api.getApiId(), 0.00);
				continue;
			}
			double avgResptime = resptimeTotal * 1.00 / callNumWithResp;
			result.put(api.getApiId(), avgResptime);
		}
		return result;
	}
	
	/**
	 * load the statistical data into LoadingCache from Redis.
	 * The key's format: <api id>_<timestamp>, such as 111_20161110090500
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
			ApiStatData returnData = null;  // the data needed to be got from LoadingCache
			
			// preparation work
			String[] strArray = ((String)key).split("-");
			int apiId = Integer.valueOf(strArray[0]);
			Date thisDate = null, endDateForPreload = null;  // start date and end date for preloading
			DateFormat compactFormat = CommonUtils.getCompactDateFormat();
			try {
				thisDate = compactFormat.parse(strArray[1]);
			} catch (ParseException ex) {
				// never happen
			}
			
			// preload data from redis
			long timeSliceNum = StatisUtils.CACHE_PRE_LOAD/AnalyzerUtils.TIME_SLICE_LENGTH;
			for (long i = 0; i < timeSliceNum; i++) {
				Date currentDate = new Date(thisDate.getTime() + i * AnalyzerUtils.TIME_SLICE_LENGTH);
				String redisKeyName = String.format("%d-%s", apiId, compactFormat.format(currentDate));

				Map<Object, Object> redisHash = template.boundHashOps(redisKeyName).entries();
				if (redisHash == null || redisHash.isEmpty()) {
					// the statistical data of the given API for the given date doesn't exist in Redis
					continue;
				}
				
				ApiStatData apiStatData = new ApiStatData();
				apiStatData.setApiId(apiId);
				apiStatData.setStartTime(currentDate);
				
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
			if (returnData != null) {
				return returnData;
			}
			
			// preload data from mysql
			endDateForPreload = new Date(thisDate.getTime() + StatisUtils.CACHE_PRE_LOAD); // end date for preloading
			List<ApiStatData> dataList = apiStatDataMapper.getDataByTime(apiId, thisDate, endDateForPreload);
			
			if (dataList.size() == 0 || dataList.get(0).getStartTime().getTime() != thisDate.getTime()) {
				//logger.debug(String.format("statistical data [%s] not found in both redis and mysql", key));
				// must not return null
				throw new ExecutionException(key + " not found in both redis and mysql", 
						new WatchdogException(ReturnCode.STAT_DATA_NOT_FOUND));
			}
			returnData = dataList.get(0);
			
			for (ApiStatData data : dataList) {
				String dataRedisKey = String.format("%d-%s", 
						data.getApiId(), 
						compactFormat.format(data.getStartTime())
					);
				dataCache.put(dataRedisKey, data);
			}
			
			return returnData;
		}
	}

}
