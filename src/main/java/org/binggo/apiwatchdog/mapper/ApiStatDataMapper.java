package org.binggo.apiwatchdog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.binggo.apiwatchdog.domain.ApiStatData;

@Mapper
public interface ApiStatDataMapper {
	
    int insert(ApiStatData apiStatData);
    
    int insertSelective(ApiStatData apiStatData);
    
}