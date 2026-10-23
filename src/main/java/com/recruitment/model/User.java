package com.recruitment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 求职者账号
 * 注册 / 登录时用 @RequestBody 直接把请求体 JSON 填进来，
 * 所以属性名必须和前端 JSON 的 key 对得上：userName / passWord / nickName。
 *
 * @JsonInclude(NON_NULL)：返回给前端时，值为 null 的字段直接不输出，
 * 这样密码被「抹掉」后响应里就不会出现 passWord 这个字段。
 *
 * 第03步：@TableName 指定表名，@TableId(AUTO) 指定主键自增。
 */
@Data
@TableName("user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;                       // 用户ID
    private String userName;               // 用户名
    private String passWord;               // 密码
    private String nickName;               // 昵称
    private String email;                  // 邮箱
    private String phone;                  // 手机号
    private LocalDateTime createTime;      // 创建时间
}
