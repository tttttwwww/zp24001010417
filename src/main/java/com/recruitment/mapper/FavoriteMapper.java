package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.model.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * favorite 表的访问入口。
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
