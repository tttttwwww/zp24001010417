package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.model.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * job 表的访问入口。
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
