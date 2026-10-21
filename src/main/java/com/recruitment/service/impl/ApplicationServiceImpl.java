package com.recruitment.service.impl;

import com.recruitment.common.Result;
import com.recruitment.data.MockDataStore;
import com.recruitment.model.Application;
import com.recruitment.model.Job;
import com.recruitment.service.ApplicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投递模块实现（按课件 4.2 编写）
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Override
    public Result<String> apply(Long userId, Long jobId) {
        // 第 1 步：两个 id 都不能为空
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：用户、职位都要存在
        Job job = MockDataStore.getJobById(jobId);
        if (MockDataStore.getUserById(userId) == null || job == null) {
            return Result.error("用户或职位不存在");
        }

        // 第 3 步：查是否已有「同一 userId + jobId」的投递记录
        Application existing = findApplication(userId, jobId);
        if (existing != null) {
            if (existing.getStatus() == Application.STATUS_APPLIED) {
                return Result.error("已投递");
            }
            // 第 4 步：曾经取消过 → 改回已投递，清空 cancelTime
            existing.setStatus(Application.STATUS_APPLIED);
            existing.setApplyTime(LocalDateTime.now());
            existing.setCancelTime(null);
            job.setApplyCount(job.getApplyCount() + 1);
            MockDataStore.saveJob(job);
            return Result.success("重新投递成功", null);
        }

        // 第 4 步：全新投递 → 新建 Application 加入列表
        Application application = new Application();
        application.setId(MockDataStore.nextApplicationId());
        application.setUserId(userId);
        application.setJobId(jobId);
        application.setStatus(Application.STATUS_APPLIED);
        application.setApplyTime(LocalDateTime.now());
        MockDataStore.getApplications().add(application);

        // 第 5 步：职位投递次数 +1
        job.setApplyCount(job.getApplyCount() + 1);
        MockDataStore.saveJob(job);

        return Result.success("投递成功", null);
    }

    @Override
    public Result<String> cancel(Long userId, Long jobId) {
        // 第 1 步：校验 id
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：只能取消「当前状态为已投递」的那条
        Application target = findApplied(userId, jobId);
        if (target == null) {
            return Result.error("未找到可取消的投递记录");
        }

        // 第 3 步：改状态、写 cancelTime（不 remove，记录还在列表里）
        target.setStatus(Application.STATUS_CANCELED);
        target.setCancelTime(LocalDateTime.now());

        // 第 4 步：职位 applyCount - 1，不低于 0
        Job job = MockDataStore.getJobById(jobId);
        if (job != null && job.getApplyCount() > 0) {
            job.setApplyCount(job.getApplyCount() - 1);
            MockDataStore.saveJob(job);
        }

        return Result.success("取消投递成功", null);
    }

    @Override
    public Result<List<Map<String, Object>>> listByUser(Long userId) {
        // 第 1 步：校验 userId
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        // 第 2 步：用户必须存在
        if (MockDataStore.getUserById(userId) == null) {
            return Result.error("用户不存在");
        }

        // 第 3 步：筛该用户的投递，拼 job 信息 + 状态 + 时间
        List<Map<String, Object>> result = new ArrayList<>();
        for (Application application : MockDataStore.getApplications()) {
            if (!userId.equals(application.getUserId())) {
                continue;
            }
            Job job = MockDataStore.getJobById(application.getJobId());
            if (job == null) {
                continue;
            }
            Map<String, Object> info = new HashMap<>();
            info.put("jobId", job.getId());
            info.put("jobTitle", job.getJobTitle());
            info.put("companyName", job.getCompanyName());
            info.put("status", application.getStatus());
            info.put("applyTime", application.getApplyTime());
            info.put("cancelTime", application.getCancelTime());
            result.add(info);
        }
        return Result.success(result);
    }

    /** 按 userId + jobId 找投递记录（任意状态），没有则 null */
    private Application findApplication(Long userId, Long jobId) {
        for (Application application : MockDataStore.getApplications()) {
            if (userId.equals(application.getUserId()) && jobId.equals(application.getJobId())) {
                return application;
            }
        }
        return null;
    }

    /** 找「已投递」状态的那一条，没有则 null */
    private Application findApplied(Long userId, Long jobId) {
        Application application = findApplication(userId, jobId);
        if (application != null && application.getStatus() == Application.STATUS_APPLIED) {
            return application;
        }
        return null;
    }
}
