package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.common.Result;
import com.recruitment.mapper.JobMapper;
import com.recruitment.model.Job;
import com.recruitment.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 职位模块实现（第03步：数据源从 MockDataStore 换成 JobMapper）
 */
@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobMapper jobMapper;

    @Override
    public Result<List<Job>> list(String city, String keyword) {
        // 用条件构造器拼 SQL：
        //   WHERE city = ?
        //     AND (company_name LIKE ? OR job_title LIKE ?)
        // 参数为空时对应条件不出现
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();

        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq(Job::getCity, city.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String realKeyword = keyword.trim();
            // and(...) 保证 OR 条件被括号包住，不会和 city 条件串味
            wrapper.and(w -> w.like(Job::getCompanyName, realKeyword)
                    .or().like(Job::getJobTitle, realKeyword));
        }
        wrapper.orderByAsc(Job::getId);

        return Result.success(jobMapper.selectList(wrapper));
    }

    @Override
    public Result<Job> detail(Long id) {
        // 第 1 步：按 id 去库里查
        Job job = jobMapper.selectById(id);
        if (job == null) {
            // 第 2 步：查不到就返回错误
            return Result.error("职位不存在");
        }

        // 第 3 步：找到了，浏览量 +1 并写回数据库（第02步只改内存，重启会丢）
        job.setViewCount(job.getViewCount() + 1);
        jobMapper.updateById(job);

        return Result.success(job);
    }
}
