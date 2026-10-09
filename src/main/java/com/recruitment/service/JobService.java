package com.recruitment.service;

import com.recruitment.common.Result;
import com.recruitment.model.Job;

import java.util.List;

public interface JobService {

    // 职位列表：city、keyword 可为空
    Result<List<Job>> list(String city, String keyword);

    // 职位详情：按 id 查；找到后浏览量 +1
    Result<Job> detail(Long id);
}
