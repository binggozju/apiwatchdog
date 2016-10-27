package org.binggo.apiwatchdog.collector;

import java.util.Properties;

public class KafkaAgentHelper {
	
	// the normal configuration items of kafka's consumer
	public static final String BOOTSTRAP_SERVERS_CONFIG = "bootstrap.servers";
	public static final String BOOTSTRAP_SERVERS_DEFAULT = "localhost:9092";
	
	public static final String GROUP_ID_CONFIG = "group.id";
	public static final String GROUP_ID_DEFAULT = "apiwatchdog";
	
	// configuration name used in apiwatchdog
	public static final String KAFKA_SERVERS_CONFIG = "kafka.bootstrap.servers";
	public static final String KAFKA_GROUP_CONFIG = "kafka.group.id";
	
	public static final String KAFKA_TOPIC_CONFIG = "kafka.topic";
	public static final String KAFKA_TOPIC_DEFAULT = "apiwatchdog-apicall";
	public static final String KAFKA_CONSUMER_NUM_CONFIG = "kafka.consumer.num";
	public static final Integer KAFKA_CONSUMER_NUM_DEFAULT = 2;
	
	public static final Long FETCH_DATA_TIMEOUT = 10L;  // seconds
	
	public static final String CONSUMER_THREAD_NAME_DEFAUTL = "Kafka-Consumer";
	
	// generate a default kafka properties
	public static Properties getDefaultProperties() {
		Properties props = new Properties();
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		return props;
	}
	
}
