package org.binggo.apiwatchdog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.binggo.apiwatchdog.domain.ApiProvider;

@Mapper
public interface ApiProviderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ApiProvider record);

    int insertSelective(ApiProvider record);

    ApiProvider selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ApiProvider record);

    int updateByPrimaryKeyWithBLOBs(ApiProvider record);

    int updateByPrimaryKey(ApiProvider record);
}