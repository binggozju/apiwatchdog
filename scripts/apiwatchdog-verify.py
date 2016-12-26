#!/usr/bin/python
# coding=utf-8

import sys
import json
import uuid
import time
from pykafka import KafkaClient

topic_name = "apiwatchdog-apicall"
kafka_hosts = "10.168.59.183:9092,10.168.72.226:9092,10.168.76.90:9092"

def main():
    callUuid = str(uuid.uuid1())
    requestTime = time.strftime("%Y-%m-%d %X", time.localtime(time.time()))
    responseTime = time.strftime("%Y-%m-%d %X", time.localtime(time.time() + 6))

    api_call = {
            "apiId": 58,
            "callUuid": callUuid,
            "requestTime": requestTime,
            "responseTime": responseTime,
            "httpResponseCode": "200",
            "apiReturnCode": "0",
            "apiReturnMessage": "internal error",
            "sourceService": "apiwatchdog监控验证",
            "sourceHost": "",
            "destService": "",
            "destHost": "",
            "requestBody": "",
            "responseBody": ""
        }
    api_call_str = json.dumps(api_call)

    client = KafkaClient(hosts=kafka_hosts)
    topic = client.topics[topic_name]

    with topic.get_sync_producer() as producer:
        producer.produce(api_call_str)

if __name__ == "__main__":
    sys.exit(main())

