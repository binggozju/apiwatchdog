package org.binggo.apiwatchdog.mapper;

import org.apache.ibatis.annotations.Mapper;

import org.binggo.apiwatchdog.domain.ApiCall;

@Mapper
public interface ApiBadCallMapper {
	
    int insert(ApiCall apiBadCall);

}