package com.recruitment.controller;

import com.recruitment.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器（按课件 7.4 / 7.5 节编写）
 * 先写几个最简单的接口，体验「浏览器访问一个地址 → 后端返回结果」
 *
 * 访问地址（注意前面有 /api，来自 application.yml 的 context-path）：
 *   http://localhost:8080/api/hello          纯欢迎语
 *   http://localhost:8080/api/hello/info     系统信息 JSON
 *   http://localhost:8080/api/hello/health   健康检查（课上练习 8.1）
 *   http://localhost:8080/api/hello/now      当前时间（课上练习 8.2）
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public Result<String> hello() {
        return Result.success("欢迎来到招聘网站！Spring Boot 3.2 + Java 17");
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("projectName", "招聘网站");
        info.put("version", "1.0.0");
        info.put("springBootVersion", "3.2.2");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("serverTime", LocalDateTime.now());
        info.put("status", "运行中");
        return Result.success(info);
    }

    /** 课上练习 8.1：健康检查 */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("app", "recruitment-website");
        return Result.success(info);
    }

    /** 课上练习 8.2：当前时间 */
    @GetMapping("/now")
    public Result<String> now() {
        return Result.success(LocalDateTime.now().toString());
    }
}
