package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recruitment.common.Result;
import com.recruitment.mapper.ApplicationMapper;
import com.recruitment.mapper.JobMapper;
import com.recruitment.mapper.UserMapper;
import com.recruitment.model.Application;
import com.recruitment.model.Job;
import com.recruitment.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投递模块实现（第03步：数据源从 MockDataStore 换成 ApplicationMapper + UserMapper + JobMapper）
 *
 * 业务规则与第02步保持一致：
 *   - 已投递不能再投（「已投递」）
 *   - 取消过的记录不删除，只把 status 改回已投递（「重新投递成功」）
 *   - 取消只改状态、不清记录
 *   - 职位的 apply_count 跟随投递 / 取消增减
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public Result<String> apply(Long userId, Long jobId) {
        // 第 1 步：两个 id 都不能为空
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：用户、职位都要存在
        Job job = jobMapper.selectById(jobId);
        if (userMapper.selectById(userId) == null || job == null) {
            return Result.error("用户或职位不存在");
        }

        // 第 3 步：查是否已有「同一 userId + jobId」的投递记录
        Application existing = findApplication(userId, jobId);
        if (existing != null) {
            if (existing.getStatus() == Application.STATUS_APPLIED) {
                return Result.error("已投递");
            }

            // 第 4 步：曾经取消过 → 改回已投递，同时清空 cancel_time
            // 注意：updateById 默认忽略 null 字段，清空 cancel_time 要用 UpdateWrapper 显式 set
            LambdaUpdateWrapper<Application> update = new LambdaUpdateWrapper<>();
            update.eq(Application::getId, existing.getId())
                    .set(Application::getStatus, Application.STATUS_APPLIED)
                    .set(Application::getApplyTime, LocalDateTime.now())
                    .set(Application::getCancelTime, null);
            applicationMapper.update(null, update);

            job.setApplyCount(job.getApplyCount() + 1);
            jobMapper.updateById(job);
            return Result.success("重新投递成功", null);
        }

        // 第 4 步：全新投递 → 新建记录（id 由数据库自增）
        Application application = new Application();
        application.setUserId(userId);
        application.setJobId(jobId);
        application.setStatus(Application.STATUS_APPLIED);
        application.setApplyTime(LocalDateTime.now());
        applicationMapper.insert(application);

        // 第 5 步：职位投递次数 +1
        job.setApplyCount(job.getApplyCount() + 1);
        jobMapper.updateById(job);

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

        // 第 3 步：改状态、写 cancelTime（不删除，记录还在表里）
        target.setStatus(Application.STATUS_CANCELED);
        target.setCancelTime(LocalDateTime.now());
        applicationMapper.updateById(target);

        // 第 4 步：职位 applyCount - 1，不低于 0
        Job job = jobMapper.selectById(jobId);
        if (job != null && job.getApplyCount() > 0) {
            job.setApplyCount(job.getApplyCount() - 1);
            jobMapper.updateById(job);
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
        if (userMapper.selectById(userId) == null) {
            return Result.error("用户不存在");
        }

        // 第 3 步：查该用户的投递记录，拼上职位信息与状态、时间
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId, userId);
        wrapper.orderByAsc(Application::getId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Application application : applicationMapper.selectList(wrapper)) {
            Job job = jobMapper.selectById(application.getJobId());
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
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId, userId);
        wrapper.eq(Application::getJobId, jobId);
        return applicationMapper.selectOne(wrapper);
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
