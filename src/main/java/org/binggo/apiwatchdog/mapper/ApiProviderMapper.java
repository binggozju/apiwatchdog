package org.binggo.apiwatchdog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import org.binggo.apiwatchdog.domain.ApiProvider;

@Mapper
public interface ApiProviderMapper {
	
	int insert(ApiProvider apiProvider);
	
    int deleteById(Integer providerId);

    int updateById(ApiProvider apiProvider);
    
    List<ApiProvider> listApiProviders();
}