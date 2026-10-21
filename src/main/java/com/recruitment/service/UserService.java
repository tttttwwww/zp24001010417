package com.recruitment.service;

import com.recruitment.common.Result;
import com.recruitment.model.User;

/**
 * 用户模块（按课件 1.1 编写）
 */
public interface UserService {

    // 注册：参数是前端传来的「原始请求 User」，返回统一响应包装的 User
    Result<User> register(User request);

    // 登录：参数是前端传来的，回 User 登录用户信息
    Result<User> login(User request);

    // 按 id 查用户信息
    Result<User> getUserInfo(Long userId);
}
