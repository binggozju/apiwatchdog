#!/bin/bash
# --------------------------------------------------------
# Usage: used to test each http api of apiwatchdog
# --------------------------------------------------------

HOST=http://localhost:8081

# -------------- config ------------------
# provider config
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"mockapp\", \"version\":\"1.0.0\", \"state\":1, \"weixinReceivers\":\"ybzhan\", \"mailReceivers\":\"ybzhan@ibenben.com;470138367@qq.com\", \"phoneReceivers\":\"18790909090\", \"description\":\"mockapp\"}" $HOST/config/provider/add 2> /dev/null
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"msgsender\", \"version\":\"1.0.1\", \"state\":1, \"weixinReceivers\":\"ybzhan\", \"mailReceivers\":\"ybzhan@ibenben.com\", \"phoneReceivers\":\"18790909090\", \"description\":\"msgsender\"}" $HOST/config/provider/add 2> /dev/null
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"test\", \"version\":\"1.0.0\", \"state\":1, \"weixinReceivers\":\"ybzhan\", \"mailReceivers\":\"ybzhan@ibenben.com\", \"phoneReceivers\":\"18790909090\", \"description\":\"test\"}" $HOST/config/provider/add 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"version\":\"2.1.0\", \"description\":\"this is mockapp\"}" $HOST/config/provider/update 2> /dev/null
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"version\":\"2.1.0\", \"description\":\"this is mockapp\"}" $HOST/config/provider/update 2> /dev/null

# curl -X DELETE -H "Accept: application/json" $HOST/config/provider/3 2> /dev/null

# curl -X GET -H "Accept: application/json" $HOST/config/provider/list 2> /dev/null
# curl -X GET -H "Accept: application/json" $HOST/config/provider/list?offset=0&size=2 2>&1 /dev/null

# api config
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"mockapp_api1\", \"path\":\"/path/to/api1\", \"type\":\"post\", \"providerId\":1, \"state\":1, \"metricNot200\":1, \"metric200Not0\":0, \"metricResptimeThreshold\":4, \"alarmType\":3}" $HOST/config/api/add 2> /dev/null
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"mockapp_api2\", \"path\":\"/path/to/api2\", \"type\":\"get\", \"providerId\":1, \"state\":1, \"metricNot200\":1, \"metric200Not0\":0, \"metricResptimeThreshold\":5, \"alarmType\":1}" $HOST/config/api/add 2> /dev/null
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"mockapp_api3\", \"path\":\"/path/to/api3\", \"type\":\"post\", \"providerId\":1, \"state\":1, \"metricNot200\":1, \"metric200Not0\":0, \"metricResptimeThreshold\":1, \"alarmType\":1}" $HOST/config/api/add 2> /dev/null
curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"name\":\"sendSyncMail\", \"path\":\"/mail/sync\", \"type\":\"post\", \"providerId\":2, \"state\":1, \"metricNot200\":1, \"metric200Not0\":0, \"metricResptimeThreshold\":5, \"alarmType\":1, \"weixinReceivers\":\"ybzhan;jjweng\"}" $HOST/config/api/add 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":1, \"name\":\"api1\"}" $HOST/config/api/update 2> /dev/null

# curl -X DELETE -H "Accept: application/json" $HOST/config/api/3 2> /dev/null

# curl -X GET -H "Accept: application/json" $HOST/config/api/list 2> /dev/null
# curl -X GET -H "Accept: application/json" $HOST/config/api/list?providerId=1 2> /dev/null
# curl -X GET -H "Accept: application/json" $HOST/config/api/list?providerId=1&offset=0&size=2 2> /dev/null

# -------------- collector ------------------
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":1, \"callUuid\": \"09A7A151-3821-2D1B-0763-B63728F16D5F\", \"requestTime\":\"2016-10-26 11:46:40\", \"sourceService\":\"clientapp\"}" $HOST/collector/collect 2> /dev/null

#curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"callUuid\": \"29A8A151-3821-2D1B-0763-B63728F16D5F\", \"requestTime\":\"2016-10-26 11:46:40\", \"responseTime\":\"2016-10-26 11:46:48\", \"httpResponseCode\":\"200\", \"apiReturnCode\":\"0\", \"sourceService\":\"clientapp\"}" $HOST/collector/collect 2> /dev/null

#curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"callUuid\": \"19A8A151-3821-2D1B-0763-B63728F16D5F\", \"requestTime\":\"2016-11-03 19:06:40\", \"responseTime\":\"2016-11-03 19:06:42\", \"httpResponseCode\":\"404\", \"apiReturnCode\":\"999\", \"sourceService\":\"clientapp\"}" $HOST/collector/collect 2> /dev/null

#curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":1, \"callUuid\": \"39A8A151-3821-2D1B-0763-B63728F16D5F\", \"requestTime\":\"2016-10-26 11:46:40\", \"responseTime\":\"2016-10-26 11:46:42\", \"httpResponseCode\":\"404\", \"apiReturnCode\":\"0\", \"sourceService\":\"clientapp\"}" $HOST/collector/collect 2> /dev/null


# -------------- admin ------------------


# -------------- statis ------------------
# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/api/getCallNumTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/api/getAvailabilityTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/api/getAccuracyTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/api/getAvgResptimeTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"apiId\":2, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/api/getResptimeDist 2> /dev/null


# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/provider/getCallNumTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/provider/getAvailabilityTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/provider/getAccuracyTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/provider/getAvgResptimeTS 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/provider/getResptimeDist 2> /dev/null


# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/ranklist/getApiAvailability 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/ranklist/getApiAccuracy 2> /dev/null

# curl -X POST -H "Accept: application/json" -H "Content-type: application/json" -d "{\"providerId\":1, \"startTime\":\"20161111111500\", \"endTime\":\"20161111115000\"}" $HOST/statis/ranklist/getApiAvgResptime 2> /dev/null


