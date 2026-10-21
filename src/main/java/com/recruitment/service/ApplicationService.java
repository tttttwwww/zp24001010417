package com.recruitment.service;

import com.recruitment.common.Result;

import java.util.List;
import java.util.Map;

/**
 * 投递模块（按课件 4.1 编写）
 * 和收藏很像，差别是取消不删记录，只改状态，所以取消用 PUT。
 */
public interface ApplicationService {

    // 投递职位：传入 userId、jobId；已投过 / 再投 / 新投由实现类区分提示
    Result<String> apply(Long userId, Long jobId);

    // 取消投递：同一对用户 id + 职位 id；只改状态，不删记录
    Result<String> cancel(Long userId, Long jobId);

    // 某用户的投递列表（Map：jobId、jobTitle、companyName、status、applyTime、cancelTime）
    Result<List<Map<String, Object>>> listByUser(Long userId);
}
