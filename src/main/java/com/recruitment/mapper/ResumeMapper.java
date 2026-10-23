package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.model.Resume;
import org.apache.ibatis.annotations.Mapper;

/**
 * resume 表的访问入口。
 */
@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}
