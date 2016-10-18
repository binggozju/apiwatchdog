package org.binggo.apiwatchdog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.binggo.apiwatchdog.domain.ApiBadCall;

@Mapper
public interface ApiBadCallMapper {
    int insert(ApiBadCall record);

    int insertSelective(ApiBadCall record);
}