package com.recruitment.config;

import com.recruitment.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 JWT 拦截器并设置白名单（第06步课件第八节）
 * 白名单必须包含 /users/login，否则“还没登录就先要 Token”，谁也登录不了。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")        // 默认拦截全部接口
                .excludePathPatterns(          // 以下地址不用 Token
                        "/",
                        "/users/login",
                        "/users/register",
                        "/jobs/**",
                        "/hello/**",
                        "/files/upload",
                        "/uploads/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/error"
                );
    }
}
