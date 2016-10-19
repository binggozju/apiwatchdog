package org.binggo.apiwatchdog.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import org.binggo.apiwatchdog.domain.ApiItem;

@Mapper
public interface ApiItemMapper {
	
	int insert(ApiItem apiItem);

    int deleteById(Integer apiId);

    int updateById(ApiItem apiItem);
    
    List<ApiItem> listApiItemsByProviderId(Integer providerId);
    
    List<ApiItem> listApiItems();
    
}