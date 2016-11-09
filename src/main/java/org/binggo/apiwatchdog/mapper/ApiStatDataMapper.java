package org.binggo.apiwatchdog.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.binggo.apiwatchdog.domain.ApiStatData;

@Mapper
public interface ApiStatDataMapper {
	
    int insert(ApiStatData apiStatData);
    
    int insertSelective(ApiStatData apiStatData);
    
    // get the statistical data from the start date to end date by API id
    List<ApiStatData> getDataByTime(@Param("apiId") Integer apiId, 
    		@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
}