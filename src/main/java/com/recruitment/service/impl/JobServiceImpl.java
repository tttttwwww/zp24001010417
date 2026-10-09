package com.recruitment.service.impl;

import com.recruitment.common.Result;
import com.recruitment.data.MockDataStore;
import com.recruitment.model.Job;
import com.recruitment.service.JobService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    @Override
    public Result<List<Job>> list(String city, String keyword) {
        // 第 1 步：准备结果列表，遍历 MockDataStore 里所有职位
        List<Job> result = new ArrayList<>();
        for (Job job : MockDataStore.getJobs()) {
            // 第 2 步：按 city 过滤——为空就不过滤
            if (!matchCity(job, city)) {
                continue;
            }
            // 第 3 步：按 keyword 过滤——为空就不过滤，匹配公司名或职位名
            if (!matchKeyword(job, keyword)) {
                continue;
            }
            result.add(job);
        }
        return Result.success(result);
    }

    @Override
    public Result<Job> detail(Long id) {
        // 第 1 步：按 id 去仓库里查
        Job job = MockDataStore.getJobById(id);
        // 第 2 步：查不到就返回错误
        if (job == null) {
            return Result.error("职位不存在");
        }
        // 第 3 步：找到了，浏览量 viewCount + 1，再返回
        job.setViewCount(job.getViewCount() + 1);
        return Result.success(job);
    }

    private boolean matchCity(Job job, String city) {
        if (city == null || city.trim().isEmpty()) {
            return true;
        }
        return city.equals(job.getCity());
    }

    private boolean matchKeyword(Job job, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String realKeyword = keyword.trim();
        return job.getCompanyName().contains(realKeyword)
                || job.getJobTitle().contains(realKeyword);
    }
}
