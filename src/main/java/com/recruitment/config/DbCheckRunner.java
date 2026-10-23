package com.recruitment.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.mapper.JobMapper;
import com.recruitment.mapper.UserMapper;
import com.recruitment.model.Job;
import com.recruitment.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 课件 3.4.6「BaseMapper 测试」的控制台演示。
 *
 * 平时启动不会执行（只有激活 dbcheck profile 才生效）：
 *   java -jar target/recruitment-system-1.0.0.jar --spring.profiles.active=dbcheck
 * 或用 Maven：
 *   mvn spring-boot:run -Dspring-boot.run.profiles=dbcheck
 *
 * 输出内容与课件要求一致：
 *   1) selectById(1L) 的 User 对象（控制台同时能看到 MyBatis 打印的 SQL）
 *   2) selectPage 第 1 页、每页 3 条的 total / pages / records（验证分页拦截器）
 *
 * 不放在启动类里，是为了保持正式代码干净：课件要求的临时测试代码不进主流程。
 */
@Component
@Profile("dbcheck")
public class DbCheckRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final JobMapper jobMapper;

    public DbCheckRunner(UserMapper userMapper, JobMapper jobMapper) {
        this.userMapper = userMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n================ BaseMapper 验证 ================");

        // 1) 按主键查一条：SELECT * FROM user WHERE id = 1
        User user = userMapper.selectById(1L);
        System.out.println("[selectById(1L)] " + user);

        // 2) 分页：第 1 页、每页 3 条（拦截器会先 COUNT 再 LIMIT）
        Page<User> page = new Page<>(1, 3);
        userMapper.selectPage(page, null);
        System.out.println("[selectPage] 总条数 = " + page.getTotal());
        System.out.println("[selectPage] 总页数 = " + page.getPages());
        System.out.println("[selectPage] 本页数据 = " + page.getRecords());

        // 3) 顺手确认职位表也走通了
        System.out.println("[job 总数] " + jobMapper.selectCount(null));
        Job job = jobMapper.selectById(1L);
        System.out.println("[selectById(1L) job] " + job.getCompanyName() + " / " + job.getJobTitle());

        System.out.println("================ 验证结束 ================\n");
    }
}
