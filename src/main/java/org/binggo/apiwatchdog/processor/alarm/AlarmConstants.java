package org.binggo.apiwatchdog.processor.alarm;

public class AlarmConstants {

	public static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.alarm.queue.capacity";
	public static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	
	public static final String ALARM_THREAD_NUM_CONFIG = "apiwatchdog.alarm.runner.num";
	public static final Integer ALARM_THREAD_NUM_DEFAULT = 2;
	
	public static final String SENDER_URL_CONFIG = "msgsender.sender.url";
	
	// if the alarm queue is empty and collector thread is idle, make it sleep for a while
	public static final Long IDLE_SLEEP_TIME = 3L; // seconds
	
	public static final String PROCESSOR_NAME = "Alarm-Processor";
	
	public static final Integer WEIXIN_ALARM_TYPE = 1 << 0;
	public static final Integer MAIL_ALARM_TYPE = 1 << 1;
	public static final Integer SMS_ALARM_TYPE = 1 << 2;
}
