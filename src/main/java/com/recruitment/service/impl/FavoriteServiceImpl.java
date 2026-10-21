package com.recruitment.service.impl;

import com.recruitment.common.Result;
import com.recruitment.data.MockDataStore;
import com.recruitment.model.Favorite;
import com.recruitment.model.Job;
import com.recruitment.service.FavoriteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏模块实现（按课件 3.2 编写）
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Override
    public Result<String> add(Long userId, Long jobId) {
        // 第 1 步：两个 id 都不能为空
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：用户、职位都要在仓库里存在
        if (MockDataStore.getUserById(userId) == null || MockDataStore.getJobById(jobId) == null) {
            return Result.error("用户或职位不存在");
        }

        // 第 3 步：同一用户 + 同一职位只能收藏一次
        if (findFavorite(userId, jobId) != null) {
            return Result.error("已收藏");
        }

        // 第 4 步：新建 Favorite，加入 getFavorites() 返回的列表
        Favorite favorite = new Favorite();
        favorite.setId(MockDataStore.nextFavoriteId());
        favorite.setUserId(userId);
        favorite.setJobId(jobId);
        favorite.setCreateTime(LocalDateTime.now());
        MockDataStore.getFavorites().add(favorite);

        return Result.success("收藏成功", null);
    }

    @Override
    public Result<String> remove(Long userId, Long jobId) {
        // 第 1 步：校验 id
        if (userId == null || jobId == null) {
            return Result.error("用户ID和职位ID不能为空");
        }

        // 第 2 步：找不到这对 userId + jobId 的收藏记录
        if (findFavorite(userId, jobId) == null) {
            return Result.error("收藏记录不存在");
        }

        // 第 3 步：从列表里删掉（收藏模块允许物理删除，和投递「改状态」不同）
        MockDataStore.getFavorites().removeIf(f ->
                userId.equals(f.getUserId()) && jobId.equals(f.getJobId()));

        return Result.success("取消收藏成功", null);
    }

    @Override
    public Result<List<Job>> listByUser(Long userId) {
        // 第 1 步：校验 userId
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        // 第 2 步：用户必须存在
        if (MockDataStore.getUserById(userId) == null) {
            return Result.error("用户不存在");
        }

        // 第 3 步：遍历收藏，按 jobId 查职位，组装成 Job 列表返回
        List<Job> jobs = new ArrayList<>();
        for (Favorite favorite : MockDataStore.getFavorites()) {
            if (!userId.equals(favorite.getUserId())) {
                continue;
            }
            Job job = MockDataStore.getJobById(favorite.getJobId());
            if (job != null) {
                jobs.add(job);
            }
        }
        return Result.success(jobs);
    }

    /** 在收藏列表里找：指定 userId + jobId 的那一条，没有则 null */
    private Favorite findFavorite(Long userId, Long jobId) {
        for (Favorite favorite : MockDataStore.getFavorites()) {
            if (userId.equals(favorite.getUserId()) && jobId.equals(favorite.getJobId())) {
                return favorite;
            }
        }
        return null;
    }
}
