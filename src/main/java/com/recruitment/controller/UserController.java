package com.recruitment.controller;

import com.recruitment.common.Result;
import com.recruitment.model.User;
import com.recruitment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块（按课件 1.3 编写）
 * 重点是 @RequestBody：参数不从 URL 取，从 POST 的 JSON 体里取，Jackson 按字段名填进 User。
 *
 * 访问地址（注意前面有 /api）：
 *   POST http://localhost:8080/api/users/register   注册
 *   POST http://localhost:8080/api/users/login      登录
 *   GET  http://localhost:8080/api/users/1          用户信息
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody User request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody User request) {
        return userService.login(request);
    }

    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable("id") Long id) {
        return userService.getUserInfo(id);
    }
}
