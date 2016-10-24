package org.binggo.apiwatchdog.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * utility class to get the configuration items from apiwatchdog.properties
 * @author Administrator
 *
 */
@Component
public class WatchdogEnv {
	
	private static final Logger logger = LoggerFactory.getLogger(WatchdogEnv.class);
	
	private Environment envConf;
	
	@Autowired
	public WatchdogEnv(Environment env) {
		this.envConf = env;
	}
	
	/**
	 * get its value according to the configuration item name whose type is Integer.
	 * @param key the name of the configuration item
	 * @param defaultValue the default value of the configuration item
	 * @return
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		Integer val = envConf.getProperty(key, Integer.class);
		if (val != null) {
			return val;
		} else {
			logger.info(String.format("configuration item [%s] isn't configured, use its default value [%d] instead.", 
					key, defaultValue));
			return defaultValue;
		}
	}
	
	
	public Long getLong(String key, Long defaultValue) {
		Long val = envConf.getProperty(key, Long.class);
		if (val != null) {
			return val;
		} else {
			logger.info(String.format("configuration item [%s] isn't configured, use its default value [%d] instead.", 
					key, defaultValue));
			return defaultValue;
		}
	}
	
	public String getString(String key, String defaultValue) {
		String val = envConf.getProperty(key);
		if (val == null) {
			return val;
		} else {
			logger.info(String.format("configuration item [%s] isn't configured, use its default value [%s] instead.", 
					key, defaultValue));
			return defaultValue;
		}
	}

}
