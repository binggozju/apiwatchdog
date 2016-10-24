package org.binggo.apiwatchdog.collector;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import org.binggo.apiwatchdog.common.WatchdogEnv;

/**
 * KafkaAgent work as a kafka consumer.
 * @author Administrator
 *
 */
@Component
public class KafkaAgent {
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaAgent.class);
	
	private static final Long FETCH_DATA_TIMEOUT = 10L;  // seconds
	
	private static final String SERVERS_CONFIG_NAME = "kafka.bootstrap.servers";
	private static final String GROUP_CONFIG_NAME = "kafka.group.id";
	private static final String CONSUMER_NUM_CONFIG_NAME = "kafka.consumer.num";
	private static final String TOPIC_CONFIG_NAME = "kafka.topic";
	
	private static final Integer CONSUMER_NUM_DEFAULT = 2;
	private static final String TOPIC_DEFAULT = "apiwatchdog-apicall";
	
	// primary kafka configuration
	private String topic;
	private String group;  // consumer group name
	private Integer consumerNum;  // the number of consumer thread
	private String kafkaServers;
	
	private Properties kafkaProperties;
	
	private static final String THREAD_NAME_DEFAUTL = "Kafka-Consumer-Thread";
	private Map<Thread, TopicConsumer> consumerThreadMap;
	
	@Autowired
	private Collector collector;
	
	private Boolean initialized = false;
	
	@Autowired
	public KafkaAgent(WatchdogEnv env) {
		topic = env.getString(TOPIC_CONFIG_NAME, TOPIC_DEFAULT);
		group = env.getString(GROUP_CONFIG_NAME, KafkaAgentUtils.GROUP_ID_DEFAULT);
		consumerNum = env.getInteger(CONSUMER_NUM_CONFIG_NAME, CONSUMER_NUM_DEFAULT);
		kafkaServers = env.getString(SERVERS_CONFIG_NAME, KafkaAgentUtils.BOOTSTRAP_SERVERS_DEFAULT);
		
		kafkaProperties = new Properties();

		consumerThreadMap = Maps.newHashMap();
	}
	
	private void initialize() {
		if (!initialized) {
			kafkaProperties = KafkaAgentUtils.getDefaultProperties();
			kafkaProperties.put(KafkaAgentUtils.BOOTSTRAP_SERVERS, kafkaServers);
			kafkaProperties.put(KafkaAgentUtils.GROUP_ID, group);
			
			initialized = true;
		}
	}
	
	@Scheduled(fixedDelay = 5*1000)
	public void runKafkaConsumer() {
		if (consumerThreadMap.size() != 0) {
			logger.info("The consumer threads have already exist");
			return;
		}
		
		initialize();	
		for (int i = 0; i < consumerNum; i++) {
			TopicConsumer topicConsumer = new TopicConsumer();
			
			Thread consumerThread = new Thread(topicConsumer);
			consumerThread.setName(String.format("%s-%d",THREAD_NAME_DEFAUTL, i));
			
			consumerThreadMap.put(consumerThread, topicConsumer);
			
			consumerThread.start();
		}
	}
	
	public void stopAllConsumers() {
		for (Map.Entry<Thread, TopicConsumer> entry : consumerThreadMap.entrySet()) {
			TopicConsumer topicConsumer = entry.getValue();
			Thread consumerThread = entry.getKey();
			
			topicConsumer.setShouldStop(true);
			topicConsumer.getKafkaConsumer().wakeup();
			
			consumerThread.interrupt();
			while (consumerThread.isAlive()) {
				try {
					consumerThread.join(500);
				} catch (InterruptedException ex) {
					logger.info("Interrupted while waiting for runner thread to exit. Exception follows.", ex);
				}
			}
			
		}
	}
	
	
	// one TopicConsumer per Thread
	private class TopicConsumer implements Runnable {
		
		private KafkaConsumer<String, String> consumer;
		private AtomicBoolean shouldStop = new AtomicBoolean(false);
		
		public TopicConsumer() {
			consumer = new KafkaConsumer<String, String>(kafkaProperties);
		}
		
		@Override
		public void run() {
			try {
				consumer.subscribe(Arrays.asList(topic));
				
				while (!shouldStop.get()) {
					ConsumerRecords<String, String> records = consumer.poll(FETCH_DATA_TIMEOUT*1000);
					
					for (ConsumerRecord<String, String> record : records) {
						collector.collect(record.value());
					}
				}
				
			} catch (WakeupException ex) {
				if (!shouldStop.get()) {
					String threadName = Thread.currentThread().getName();
					logger.warn(String.format("the kafka consumer thread [%s] has been shutdown.", threadName));
				}
				
			} finally {
				consumer.close();
			}
		}

		public void setShouldStop(Boolean stop) {
			shouldStop.set(stop);
		}
		
		public KafkaConsumer<String, String> getKafkaConsumer() {
			return consumer;
		}
	}

}
