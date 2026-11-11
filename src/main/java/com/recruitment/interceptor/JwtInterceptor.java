package com.recruitment.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.common.Result;
import com.recruitment.context.UserContext;
import com.recruitment.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器（第06步课件第七节）
 * 请求到达 Controller 之前先经过它：
 * preHandle        —— 读取 Token、验证身份，返回 true 才放行
 * afterCompletion  —— 请求处理完成后清除 ThreadLocal 中的用户信息
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    // Controller 执行前调用。返回 true 才放行
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        // 第 1 步：从标准 Authorization 请求头取登录凭证
        // JWT 通常按通用格式传递：Authorization: Bearer Token字符串
        String authHeader = request.getHeader("Authorization");

        // 第 2 步：必须是 Bearer + 空格 + Token startsWith
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            returnUnauthorized(response, "请先登录");
            return false;
        }

        // 第 3 步：去掉前 7 个字符“Bearer ”，只留下 Token
        String token = authHeader.substring(7);

        // 第 4 步：解析 userId，放进本次请求的 UserContext
        // validateToken 会验签并检查是否过期
        if (!jwtUtil.validateToken(token)) {
            returnUnauthorized(response, "Token无效或已过期");
            return false;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            returnUnauthorized(response, "Token无效或已过期");
            return false;
        }
        UserContext.setCurrentUserId(userId);

        // 第 5 步：验证通过，放行到 Controller
        return true;
    }

    // 请求结束后调用
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        UserContext.clear();
    }

    // 被拦住时没有 Controller 帮我们返回 Result，只能直接写 HTTP 响应
    private void returnUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.error(401, message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
