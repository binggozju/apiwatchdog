package org.binggo.apiwatchdog.collector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.binggo.apiwatchdog.Collector;
import org.binggo.apiwatchdog.Event;
import org.binggo.apiwatchdog.TimerRunnable;
import org.binggo.apiwatchdog.WatchdogRunner;
import org.binggo.apiwatchdog.common.ReturnCode;
import org.binggo.apiwatchdog.common.WatchdogEnv;
import org.binggo.apiwatchdog.common.WatchdogException;
import org.binggo.apiwatchdog.domain.ApiCall;

/**
 * KafkaAgent acts as a kafka consumer.
 * @author Binggo
 */
@Component
public class KafkaAgent implements TimerRunnable {
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaAgent.class);
	
	// primary kafka configuration
	private String topic;
	private String group;  // consumer group name
	private Integer consumerNum;  // the number of consumer thread
	private String kafkaServers;
	
	private Properties kafkaProperties;
	private Map<Thread, ConsumerRunner> consumerThreadMap;
	
	private Collector collector;
	private Gson gson;
	
	private Boolean initialized = false;
	
	@Autowired
	public KafkaAgent(WatchdogEnv env, @Qualifier("simpleCollector") Collector collector) {
		topic = env.getString(KafkaAgentHelper.KAFKA_TOPIC_CONFIG, KafkaAgentHelper.KAFKA_TOPIC_DEFAULT);
		group = env.getString(KafkaAgentHelper.KAFKA_GROUP_CONFIG, KafkaAgentHelper.GROUP_ID_DEFAULT);
		consumerNum = env.getInteger(KafkaAgentHelper.KAFKA_CONSUMER_NUM_CONFIG, KafkaAgentHelper.KAFKA_CONSUMER_NUM_DEFAULT);
		kafkaServers = env.getString(KafkaAgentHelper.KAFKA_SERVERS_CONFIG, KafkaAgentHelper.BOOTSTRAP_SERVERS_DEFAULT);
		
		kafkaProperties = new Properties();
		consumerThreadMap = Maps.newHashMap();
		
		this.collector = collector;
		this.gson = new GsonBuilder().disableHtmlEscaping().create();
	}
	
	private void initialize() {
		if (!initialized) {
			kafkaProperties = KafkaAgentHelper.getDefaultProperties();
			kafkaProperties.put(KafkaAgentHelper.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
			kafkaProperties.put(KafkaAgentHelper.GROUP_ID_CONFIG, group);
			
			initialized = true;
		}
	}
	
	@Scheduled(initialDelay=1000, fixedDelay=5*1000)
	@Override
	public void runTimerTask() {
		// create the consumer threads
		if (consumerThreadMap.size() == 0) {
			initialize();
			for (int i = 0; i < consumerNum; i++) {
				try {
					ConsumerRunner consumerRunner = new ConsumerRunner();
					Thread consumerThread = new Thread(consumerRunner);
					
					consumerThread.setName(String.format("%s-%d",KafkaAgentHelper.CONSUMER_THREAD_NAME_DEFAUTL, i));
					
					consumerThreadMap.put(consumerThread, consumerRunner);
					logger.info(String.format("start the kafka consumer thread [%s]", consumerThread.getName()));
					consumerThread.start();
					
				} catch (WatchdogException ex) {
					logger.error(String.format("%s. check your kafka configuarion information again please.", ex.getMessage()));
					return;
				}
			}
			
			return;
		}
		
		// clear the terminated threads
		Map<Thread, ConsumerRunner> cacheMap = Maps.newHashMap();
		
		Iterator<Entry<Thread, ConsumerRunner>> it = consumerThreadMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Thread, ConsumerRunner> entry = it.next();
			ConsumerRunner consumerRunner = entry.getValue();
			Thread consumerThread = entry.getKey();
			
			if (!consumerRunner.shouldStop() && !consumerThread.isAlive()) {
				logger.warn(String.format("Thread [%s] is not alive, restart it", consumerThread.getName()));
				
				// create a new consumer thread, and start it
				Thread newConsumerThread = new Thread(consumerRunner);
				newConsumerThread.setName(consumerThread.getName());
				cacheMap.put(newConsumerThread, consumerRunner);
				newConsumerThread.start();
				
				it.remove();
			}
		}
		consumerThreadMap.putAll(cacheMap);
	}
	
	public void stopAllConsumers() {
		for (Map.Entry<Thread, ConsumerRunner> entry : consumerThreadMap.entrySet()) {
			ConsumerRunner consumerRunner = entry.getValue();
			Thread consumerThread = entry.getKey();
			
			consumerRunner.setShouldStop(true);
			consumerRunner.getKafkaConsumer().wakeup();
			
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
	
	private class ConsumerRunner extends WatchdogRunner {
		
		private KafkaConsumer<String, String> consumer;
		
		public ConsumerRunner() {
			try {
				consumer = new KafkaConsumer<String, String>(kafkaProperties);
			} catch (Exception ex) {
				throw new WatchdogException(ReturnCode.FAIL_CONSTRUCT_CONSUMER, ex.getMessage());
			}
		}

		@Override
		public void run() {
			try {
				consumer.subscribe(Arrays.asList(topic));
				logger.info(String.format("Thread [%s] is running, start to consume message from kafka.", 
					Thread.currentThread().getName()));
				
				while (!shouldStop()) {
					ConsumerRecords<String, String> records = consumer.poll(500);
					for (ConsumerRecord<String, String> record : records) {
						try {
							ApiCall apiCall = gson.fromJson(record.value(), ApiCall.class);
							collector.collect(Event.buildEvent(apiCall));
						} catch (JsonSyntaxException ex) {
							logger.error(String.format("the api call of json format is invalid: %s", record.value()));
							continue;
						}	
					}
				}
			} catch (WakeupException ex) {
				if (!shouldStop()) {
					String threadName = Thread.currentThread().getName();
					logger.warn(String.format("the kafka consumer thread [%s] has been shutdown.", threadName));
				}
			} finally {
				consumer.close();
			}
		}
		
		public KafkaConsumer<String, String> getKafkaConsumer() {
			return consumer;
		}
	}

}
