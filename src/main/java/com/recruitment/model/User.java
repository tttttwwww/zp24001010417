package com.recruitment.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 求职者账号
 * 后面会用于注册、登录
 */
@Data
public class User {
    private Long id;                       // 用户ID
    private String username;               // 用户名
    private String password;               // 密码
    private String nickname;               // 昵称
    private String email;                  // 邮箱
    private String phone;                  // 手机号
    private LocalDateTime createtime;      // 创建时间
}
