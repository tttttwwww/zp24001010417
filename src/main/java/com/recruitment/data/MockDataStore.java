package com.recruitment.data;

import com.recruitment.model.Application;
import com.recruitment.model.Favorite;
import com.recruitment.model.Job;
import com.recruitment.model.Resume;
import com.recruitment.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存仓库：本步不接数据库，用线程安全的 Map / List 临时保存数据
 * 静态块里预置：demo 用户（userId = 1）和 3 条职位（jobId = 1、2、3）
 */
public final class MockDataStore {

    // ------------ 用户：按 id 存 ------------
    private static final Map<Long, User> USERS = new ConcurrentHashMap<>();
    private static final AtomicLong USER_ID = new AtomicLong(0);

    // ------------ 职位：id 固定为 1、2、3，不会新增 ------------
    private static final Map<Long, Job> JOBS = new ConcurrentHashMap<>();

    // ------------ 简历：按 id 存 ------------
    private static final Map<Long, Resume> RESUMES = new ConcurrentHashMap<>();
    private static final AtomicLong RESUME_ID = new AtomicLong(0);

    // ------------ 收藏 / 投递：直接用列表，没有单独按 id 查的需求 ------------
    private static final List<Favorite> FAVORITES = new CopyOnWriteArrayList<>();
    private static final AtomicLong FAVORITE_ID = new AtomicLong(0);

    private static final List<Application> APPLICATIONS = new CopyOnWriteArrayList<>();
    private static final AtomicLong APPLICATION_ID = new AtomicLong(0);

    private MockDataStore() {
        // 工具类，不允许 new
    }

    static {
        seedUsers();
        seedJobs();
    }

    // ============ 用户 ============

    public static Long nextUserId() {
        return USER_ID.incrementAndGet();
    }

    public static void saveUser(User user) {
        USERS.put(user.getId(), user);
    }

    public static User getUserById(Long id) {
        return id == null ? null : USERS.get(id);
    }

    public static User getUserByUsername(String username) {
        if (username == null) {
            return null;
        }
        for (User user : USERS.values()) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    public static int getUserCount() {
        return USERS.size();
    }

    private static void seedUsers() {
        User demo = new User();
        demo.setId(nextUserId());
        demo.setUsername("demo");
        demo.setPassword("123456");
        demo.setNickname("示例用户");
        demo.setEmail("demo@example.com");
        demo.setPhone("13800000000");
        demo.setCreatetime(LocalDateTime.now());
        saveUser(demo);
    }

    // ============ 职位 ============

    public static List<Job> getJobs() {
        return new ArrayList<>(JOBS.values());
    }

    public static Job getJobById(Long id) {
        return id == null ? null : JOBS.get(id);
    }

    public static void saveJob(Job job) {
        JOBS.put(job.getId(), job);
    }

    private static void seedJobs() {
        saveJob(Job.builder()
                .id(1L)
                .companyName("阿里巴巴")
                .jobTitle("Java开发工程师")
                .jobType("全职")
                .salary("25-35K")
                .city("杭州")
                .address("杭州市余杭区")
                .experience("3-5年")
                .education("本科")
                .jobDesc("负责电商核心系统的开发与维护")
                .jobRequirement("熟悉 Spring Boot、MySQL、Redis")
                .welfare("五险一金、年终奖、免费三餐")
                .contactName("张经理")
                .contactPhone("0571-88888888")
                .viewCount(0)
                .applyCount(0)
                .build());

        saveJob(Job.builder()
                .id(2L)
                .companyName("腾讯科技")
                .jobTitle("前端开发工程师")
                .jobType("全职")
                .salary("20-30K")
                .city("深圳")
                .address("深圳市南山区")
                .experience("1-3年")
                .education("本科")
                .jobDesc("负责社交产品前端页面开发")
                .jobRequirement("熟悉 Vue、React 任一框架")
                .welfare("五险一金、股票期权")
                .contactName("李经理")
                .contactPhone("0755-88888888")
                .viewCount(0)
                .applyCount(0)
                .build());

        saveJob(Job.builder()
                .id(3L)
                .companyName("字节跳动")
                .jobTitle("Go开发工程师")
                .jobType("全职")
                .salary("30-45K")
                .city("北京")
                .address("北京市海淀区")
                .experience("3-5年")
                .education("本科")
                .jobDesc("负责高并发中间件研发")
                .jobRequirement("熟悉 Go、微服务架构")
                .welfare("六险一金、免费班车")
                .contactName("王经理")
                .contactPhone("010-88888888")
                .viewCount(0)
                .applyCount(0)
                .build());
    }

    // ============ 简历 ============

    public static Long nextResumeId() {
        return RESUME_ID.incrementAndGet();
    }

    public static void saveResume(Resume resume) {
        RESUMES.put(resume.getId(), resume);
    }

    public static Resume getResumeById(Long id) {
        return id == null ? null : RESUMES.get(id);
    }

    public static void deleteResume(Long id) {
        if (id != null) {
            RESUMES.remove(id);
        }
    }

    public static List<Resume> getResumesByUserId(Long userId) {
        List<Resume> result = new ArrayList<>();
        for (Resume resume : RESUMES.values()) {
            if (resume.getUserId() != null && resume.getUserId().equals(userId)) {
                result.add(resume);
            }
        }
        return result;
    }

    // ============ 收藏 ============

    public static Long nextFavoriteId() {
        return FAVORITE_ID.incrementAndGet();
    }

    public static List<Favorite> getFavorites() {
        return FAVORITES;
    }

    // ============ 投递 ============

    public static Long nextApplicationId() {
        return APPLICATION_ID.incrementAndGet();
    }

    public static List<Application> getApplications() {
        return APPLICATIONS;
    }
}
