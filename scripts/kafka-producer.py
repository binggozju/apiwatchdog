#!/usr/bin/python                                                                                    

import sys
import json
from pykafka import KafkaClient

topic_name = "apiwatchdog-apicall"
kafka_hosts = "192.168.106.129:9092"
zk_hosts = "192.168.106.129:2181"

def main():
    api_call = {"apiId": 2,
                "callUuid": "49E4D4B2-3F5D-5EF5-E4FF-5E24DAC1BA5E",
                "requestTime": "2016-11-07 10:46:32",
                "responseTime": "2016-11-07 10:46:35",
                "httpResponseCode": "200",
                "apiReturnCode": "99999",
                "apiReturnMessage": "internal error",
                "sourceService": "mockservice",
                "sourceHost": "", 
                "destService": "", 
                "destHost": "", 
                "requestBody": "", 
                "responseBody": ""
            }   
    api_call_str = json.dumps(api_call)
    print api_call_str

    client = KafkaClient(hosts=kafka_hosts)
    topic = client.topics[topic_name]
    
    with topic.get_sync_producer() as producer:
        producer.produce(api_call_str)
    
    print "send %s" % (api_call)


if __name__ == "__main__":
    sys.exit(main())