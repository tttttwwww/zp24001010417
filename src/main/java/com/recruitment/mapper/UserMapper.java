package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * user 表的访问入口。
 * 继承 BaseMapper 后，selectById / insert / updateById / deleteById /
 * selectList(wrapper) / selectPage(page, wrapper) 等方法都由框架生成。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
