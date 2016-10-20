package org.binggo.apiwatchdog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import org.binggo.apiwatchdog.config.ProviderConfiguration;
import org.binggo.apiwatchdog.domain.ApiProvider;

@Mapper
public interface ApiProviderMapper {
	
	// for ConfigController and ConfigService
	int insert(ApiProvider apiProvider);
	
    int deleteById(Integer providerId);

    int updateById(ApiProvider apiProvider);
    
    List<ApiProvider> listApiProviders();
    
    // for ProviderConfiguration
    List<ProviderConfiguration> listProviderConf();
    
}