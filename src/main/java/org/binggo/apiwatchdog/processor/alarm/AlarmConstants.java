package org.binggo.apiwatchdog.processor.alarm;

public class AlarmConstants {

	public static final String QUEUE_CAPACITY_CONFIG = "apiwatchdog.alarm.queue.capacity";
	public static final Integer QUEUE_CAPACITY_DEFAULT = 10000;
	
	public static final String ALARM_THREAD_NUM_CONFIG = "apiwatchdog.alarm.runner.num";
	public static final Integer ALARM_THREAD_NUM_DEFAULT = 2;
	
	public static final String ALARM_WEIXIN_QUOTA_CONFIG = "apiwatchdog.alarm.weixin.quota";
	public static final Integer ALARM_WEIXIN_QUOTA_DEFAULT = 10;  // the quota every two hours
	
	public static final String ALARM_MAIL_QUOTA_CONFIG = "apiwatchdog.alarm.mail.quota";
	public static final Integer ALARM_MAIL_QUOTA_DEFAULT = 1; // the quota every two hours
	
	public static final String ALARM_SMS_QUOTA_CONFIG = "apiwatchdog.alarm.sms.quota";
	public static final Integer ALARM_SMS_QUOTA_DEFAULT = 0; // the quota every two hours
	
	public static final String WEIXIN_ALARM_COUNTER_NAME = "weixin_alarm_counter";
	public static final String MAIL_ALARM_COUNTER_NAME = "mail_alarm_counter";
	public static final String SMS_ALARM_COUNTER_NAME = "sms_alarm_counter";
	
	public static final Integer SCHEDULE_POOL_SIZE_DEFAULT = 1;
	public static final Integer RESET_COUNTER_SECONDS_DEFAULT = 2*60*60;  // seconds
	
	public static final String SENDER_URL_CONFIG = "msgsender.sender.url";
	
	public static final String PROCESSOR_NAME = "Alarm-Processor";
	
	public static final Integer WEIXIN_ALARM_TYPE = 1 << 0;
	public static final Integer MAIL_ALARM_TYPE = 1 << 1;
	public static final Integer SMS_ALARM_TYPE = 1 << 2;
}
