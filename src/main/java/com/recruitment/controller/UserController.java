package com.recruitment.controller;

import com.recruitment.common.Result;
import com.recruitment.context.UserContext;
import com.recruitment.model.User;
import com.recruitment.service.UserService;
import com.recruitment.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块（按课件 1.3 编写，第06步加入 JWT）
 * 重点是 @RequestBody：参数不从 URL 取，从 POST 的 JSON 体里取，Jackson 按字段名填进 User。
 *
 * 访问地址（注意前面有 /api）：
 *   POST http://localhost:8080/api/users/register   注册（白名单）
 *   POST http://localhost:8080/api/users/login      登录，返回 token + user（白名单）
 *   GET  http://localhost:8080/api/users/info       当前用户，需 Authorization: Bearer Token
 *   GET  http://localhost:8080/api/users/1          用户信息（需 Token）
 */
@Tag(name = "用户模块", description = "注册、登录（JWT）与当前用户")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "用户名重复返回「用户名已存在」")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User request) {
        return userService.register(request);
    }

    @Operation(summary = "用户登录", description = "登录成功后返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody User request) {
        return userService.login(request);
    }

    @Operation(summary = "当前用户信息", description = "根据 Token 查询当前登录用户")
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        // 不再由前端传 userId，而是拦截器从 Token 解析后放进 UserContext
        Long userId = UserContext.getCurrentUserId();
        return userService.getUserInfo(userId);
    }

    @Operation(summary = "按 id 查询用户", description = "需要携带 Token")
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable("id") Long id) {
        return userService.getUserInfo(id);
    }
}
