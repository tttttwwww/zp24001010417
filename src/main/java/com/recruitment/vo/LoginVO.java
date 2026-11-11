package com.recruitment.vo;

import com.recruitment.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应对象（第06步课件 5.1）
 * VO（View Object）是专门给接口返回的数据结构，不对应数据库表。
 * 登录成功后同时返回 Token 和已去掉密码的用户信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private User user;
}
