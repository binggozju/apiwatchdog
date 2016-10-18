package org.binggo.apiwatchdog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.binggo.apiwatchdog.domain.ApiItem;

@Mapper
public interface ApiItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ApiItem record);

    int insertSelective(ApiItem record);

    ApiItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ApiItem record);

    int updateByPrimaryKeyWithBLOBs(ApiItem record);

    int updateByPrimaryKey(ApiItem record);
}