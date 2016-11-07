package org.binggo.apiwatchdog.dump;

public class DataDumperUtils {
	
	public static final String DATADUMPER_RUN_PERIOD_CONFIG = "apiwatchdog.datadumper.run.period";
	public static final Integer DATADUMPER_RUN_PERIOD_DEFAULT = 6;  // minutes
	
	public static final String DATA_DUMPER_NAME = "Data-Dumper";
	
	
	// redis
	public static final String KEY_NEXT_DUMP_TIME_SLICE = "nextDumpTimeSlice";
	
	
	// zookeeper
	public static final String ZK_CONNECT_CONFIG = "zookeeper.connect";
	public static final String ZK_CONNECT_DEFAULT = "";
	
	public static final Integer ZK_SESSION_TIMEOUT = 60; // seconds
	public static final Integer ZK_CONNECTION_TIMEOUT = 6; // seconds
	
	public static final Integer ZK_MAX_RETRY = 3;
	public static final Integer ZK_BASE_SLEEP_TIME = 5; // seconds
	public static final String ZK_LEADER_PATH = "/apiwatchdog/leader";
	
}
