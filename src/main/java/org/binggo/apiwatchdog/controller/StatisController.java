package org.binggo.apiwatchdog.controller;

import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.binggo.apiwatchdog.common.CommonUtils;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.common.WatchdogResponse;
import org.binggo.apiwatchdog.statis.Statis;

/**
 * get the statistical data for graphical display
 * @author Binggo
 */
@RestController
@RequestMapping("/statis")
public class StatisController {
	
	private static final Logger logger = LoggerFactory.getLogger(StatisController.class);
	
	@Autowired
	private Statis statis;
	
	/////////// get statistical data for the single API /////////////////////
	/**
	 * get the time series data of calling number between the start time and the end time
	 * according to a given API id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/api/getCallNumTS")
	public WatchdogResponse getApiCallNumTS(@RequestBody Map<String, String> params) {
		try {
			Integer apiId = Integer.valueOf(params.get("apiId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Integer> statisData = statis.getApiCallNumTimeSeries(apiId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * get the time series data of calling availability between the start time and the end time
	 * according to a given API id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/api/getAvailabilityTS")
	public WatchdogResponse getApiAvailablityTS(@RequestBody Map<String, String> params) {
		try {
			Integer apiId = Integer.valueOf(params.get("apiId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Double> statisData = statis.getApiAvailabilityTimeSeries(apiId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * get the time series data of calling accuracy between the start time and the end time
	 * according to a given API id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/api/getAccuracyTS")
	public WatchdogResponse getApiAccuracyTS(@RequestBody Map<String, String> params) {
		try {
			Integer apiId = Integer.valueOf(params.get("apiId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Double> statisData = statis.getApiAccuracyTimeSeries(apiId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * get the time series data of average response time between the start time and the end time
	 * according to a given API id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/api/getAvgResptimeTS")
	public WatchdogResponse getApiAvgResptimeTS(@RequestBody Map<String, String> params) {
		try {
			Integer apiId = Integer.valueOf(params.get("apiId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Double> statisData = statis.getApiAvgResptimeTimeSeries(apiId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}	
	}
	
	/**
	 * get the distribution of response time between the start time and the end time
	 * according to a given API id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/api/getResptimeDist")
	public WatchdogResponse getApiResptimeDist(@RequestBody Map<String, String> params) {
		try {
			Integer apiId = Integer.valueOf(params.get("apiId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Integer> statisData = statis.getApiResptimeDistribution(apiId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	
	/////////// get statistical data for the API provider /////////////////////
	/**
	 * get the time series data of calling number between the start time and the end time
	 * according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/provider/getCallNumTS")
	public WatchdogResponse getCallNumTS(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Integer> statisData = statis.getCallNumTimeSeries(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * get the time series data of calling availability between the start time and the end time
	 * according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/provider/getAvailabilityTS")
	public WatchdogResponse getAvailabilityTS(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Double> statisData = statis.getAvailabilityTimeSeries(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * get the time series data of calling accuracy between the start time and the end time
	 * according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/provider/getAccuracyTS")
	public WatchdogResponse getAccuracyTS(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Double> statisData = statis.getAccuracyTimeSeries(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);	
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}	
	}
	
	/**
	 * get the time series data of average response time between the start time and the end time
	 * according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/provider/getAvgResptimeTS")
	public WatchdogResponse getAvgResptimeTS(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Double> statisData = statis.getAvgResptimeTimeSeries(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}
	
	/**
	 * get the distribution of response time between the start time and the end time
	 * according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/provider/getResptimeDist")
	public WatchdogResponse getResptimeDist(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<String, Integer> statisData = statis.getResptimeDistribution(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, statisData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}
	}

	
	/////////// get the rank list of API for the API provider /////////////////////
	/**
	 * get the rank list data of all API by their availability 
	 * between the start time and the end time according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/ranklist/getApiAvailability")
	public WatchdogResponse getApiRankListByAvailablity(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<Integer, Double> rankListData = statis.getApiRankListByAvailablity(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, rankListData);	
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}	
	}

	/**
	 * get the rank list data of all API by their accuracy 
	 * between the start time and the end time according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/ranklist/getApiAccuracy")
	public WatchdogResponse getApiRankListByAccuracy(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<Integer, Double> rankListData = statis.getApiRankListByAccuracy(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, rankListData);	
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}	
	}
	
	/**
	 * get the rank list data of all API by their average response time 
	 * between the start time and the end time according to a given provider (service) id.
	 * @param params
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST, value="/ranklist/getApiAvgResptime")
	public WatchdogResponse getApiRankListByAvgResptime(@RequestBody Map<String, String> params) {
		try {
			Integer providerId = Integer.valueOf(params.get("providerId"));
			String startTime = params.get("startTime");
			String endTime = params.get("endTime");
			Map<Integer, Double> rankListData = statis.getApiRankListByAvgResptime(providerId, startTime, endTime);
			return WatchdogResponse.getResponse(ReturnCode.OK, rankListData);
		} catch (WatchdogException ex) {
			logger.error(ex.getMessage());
			return WatchdogResponse.getResponse(ex.getReturnCode());
		}	
	}
	
}
