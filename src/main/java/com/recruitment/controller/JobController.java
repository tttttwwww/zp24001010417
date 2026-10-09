package com.recruitment.controller;

import com.recruitment.common.Result;
import com.recruitment.model.Job;
import com.recruitment.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 职位模块（按课件 5.3 编写）
 * 访问地址（注意前面有 /api）：
 *   http://localhost:8080/api/jobs/list                职位列表（3 条）
 *   http://localhost:8080/api/jobs/list?city=杭州      按城市过滤
 *   http://localhost:8080/api/jobs/list?keyword=Java   按关键词过滤
 *   http://localhost:8080/api/jobs/1                   职位详情（viewCount + 1）
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/list")
    public Result<List<Job>> list(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return jobService.list(city, keyword);
    }

    @GetMapping("/{id}")
    public Result<Job> detail(@PathVariable Long id) {
        return jobService.detail(id);
    }
}
