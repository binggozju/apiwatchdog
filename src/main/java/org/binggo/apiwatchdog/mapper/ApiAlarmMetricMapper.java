package org.binggo.apiwatchdog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.binggo.apiwatchdog.domain.ApiAlarmMetric;

@Mapper
public interface ApiAlarmMetricMapper {
    int insert(ApiAlarmMetric record);

    int insertSelective(ApiAlarmMetric record);
}