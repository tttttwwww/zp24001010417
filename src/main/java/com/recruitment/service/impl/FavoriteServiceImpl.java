package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.common.Result;
import com.recruitment.mapper.FavoriteMapper;
import com.recruitment.mapper.JobMapper;
import com.recruitment.mapper.UserMapper;
import com.recruitment.model.Favorite;
import com.recruitment.model.Job;
import com.recruitment.model.User;
import com.recruitment.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏模块实现（第03步：数据源从 MockDataStore 换成 FavoriteMapper + UserMapper + JobMapper）
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JobMapper jobMapper;

    @Override
    public Result<String> add(Long userId, Long jobId) {
        // 第 1 步：两个 id 都不能为空
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：用户、职位都要存在
        User user = userMapper.selectById(userId);
        Job job = jobMapper.selectById(jobId);
        if (user == null || job == null) {
            return Result.error("用户或职位不存在");
        }

        // 第 3 步：同一用户 + 同一职位只能收藏一次
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getJobId, jobId);
        if (favoriteMapper.selectOne(wrapper) != null) {
            return Result.error("已收藏");
        }

        // 第 4 步：新建记录，插入数据库
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setJobId(jobId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);

        return Result.success("收藏成功", null);
    }

    @Override
    public Result<String> remove(Long userId, Long jobId) {
        // 第 1 步：校验 id
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：找到这条收藏记录
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getJobId, jobId);
        Favorite target = favoriteMapper.selectOne(wrapper);
        if (target == null) {
            return Result.error("收藏记录不存在");
        }

        // 第 3 步：按主键删除（收藏允许物理删除，和投递「改状态」不同）
        favoriteMapper.deleteById(target.getId());
        return Result.success("取消收藏成功", null);
    }

    @Override
    public Result<List<Job>> listByUser(Long userId) {
        // 第 1 步：校验 userId
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        // 第 2 步：用户必须存在
        if (userMapper.selectById(userId) == null) {
            return Result.error("用户不存在");
        }

        // 第 3 步：先查该用户的收藏记录，再按 jobId 逐条查职位，组装成 Job 列表
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.orderByAsc(Favorite::getId);

        List<Job> jobs = new ArrayList<>();
        for (Favorite favorite : favoriteMapper.selectList(wrapper)) {
            Job job = jobMapper.selectById(favorite.getJobId());
            if (job != null) {
                jobs.add(job);
            }
        }
        return Result.success(jobs);
    }
}
