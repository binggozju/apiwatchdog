package org.binggo.apiwatchdog.collector;

import java.util.Properties;

public class KafkaAgentUtils {
	
	// the normal configuration items of kafka's consumer
	public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
	public static final String BOOTSTRAP_SERVERS_DEFAULT = "localhost:9092";
	
	public static final String GROUP_ID = "group.id";
	public static final String GROUP_ID_DEFAULT = "apiwatchdog";
	
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
