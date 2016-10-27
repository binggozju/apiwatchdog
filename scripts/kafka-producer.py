#!/usr/bin/python

import sys
from pykafka import kafkaClient

topic_name = "apiwatchdog-apicall"
kafka_hosts = "127.0.0.1:9092,127.0.0.1:9093"
zk_hosts = "127.0.0.1:2181"

#kafka_hosts = "10.168.72.226:9092,10.168.76.90:9092,10.168.59.183:9092"
#zk_hosts = "10.168.72.226:2181,10.168.76.90:2181,10.168.59.183:2181"

def main():
    client = KafkaClient(hosts=kafka_hosts)
    topic = client.topics[topic_name]
    print "now you can send message to topic '%s'" % (topic_name)
    with topic.get_sync_producer() as producer:
        while True:
            msg = raw_input(">> ")
            producer.produce(msg)


if __name__ == "__main__":
    sys.exit(main())

