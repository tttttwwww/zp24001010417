package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.model.Application;
import org.apache.ibatis.annotations.Mapper;

/**
 * application 表的访问入口。
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {
}
