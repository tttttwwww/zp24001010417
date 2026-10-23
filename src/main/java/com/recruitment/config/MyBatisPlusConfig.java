package com.recruitment.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第03步：MyBatis Plus 配置
 *
 * PaginationInnerInterceptor 只在调用 selectPage 时介入：
 * 先执行 COUNT 统计总数，再给查询语句自动加上 MySQL 的 LIMIT。
 * 不装这个拦截器，selectPage 拿到的 total 会是 0、也不会真的分页。
 */
@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 指定数据库类型为 MySQL，分页拦截器才知道用 LIMIT 语法
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
