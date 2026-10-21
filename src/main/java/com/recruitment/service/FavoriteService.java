package com.recruitment.service;

import com.recruitment.common.Result;
import com.recruitment.model.Job;

import java.util.List;

/**
 * 收藏模块（按课件 3.1 编写）
 */
public interface FavoriteService {

    // 收藏职位：传入 userId、jobId
    Result<String> add(Long userId, Long jobId);

    // 取消收藏：同一对用户 id + 职位 id
    Result<String> remove(Long userId, Long jobId);

    // 某用户收藏的全部职位列表（返回 Job，不是 Favorite）
    Result<List<Job>> listByUser(Long userId);
}
