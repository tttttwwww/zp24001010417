package com.recruitment.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历，靠 userId 挂到用户
 */
@Data
public class Resume {
    private Long id;                      // 简历ID
    private Long userId;                  // 所属用户ID
    private String realName;              // 真实姓名
    private Integer gender;               // 性别
    private String avatar;                // 头像
    private String phone;                 // 手机号
    private String email;                 // 邮箱
    private String city;                  // 所在城市
    private String education;             // 学历
    private String experience;            // 工作经验
    private String expectedSalary;        // 期望薪资
    private String skills;                // 技能
    private String introduction;          // 自我介绍
    private LocalDateTime createTime;     // 创建时间
    private LocalDateTime updateTime;     // 更新时间
}
