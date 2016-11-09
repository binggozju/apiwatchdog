package org.binggo.apiwatchdog.statis;

public class StatisUtils {
	
	// If a key in LoadingCache has not been accessed, it will be removed automatically.
	public static final long CACHE_EXPIRE_TIME = 6*60; // minutes
	
	// If the given key doesn't exist in LoadingCache, 
	// preload the statistical data from now to CACHE_PRE_LOAD hours later.
	public static final long CACHE_PRE_LOAD = 4*60*60*1000;  // milliseconds

}
