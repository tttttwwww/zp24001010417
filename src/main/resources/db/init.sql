-- ============================================================
-- 第03步：MySQL 初始化脚本
-- 位置：src/main/resources/db/init.sql
-- 执行后在 recruitment_db 里建 5 张表，并写入演示数据：
--   user 8 行（demo 为 id=1）、job 3 行
-- ============================================================
CREATE DATABASE IF NOT EXISTS recruitment_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE recruitment_db;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_name` VARCHAR(50) NOT NULL,
  `pass_word` VARCHAR(100) NOT NULL,
  `nick_name` VARCHAR(50) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `company_name` VARCHAR(100) NOT NULL,
  `company_logo` VARCHAR(255) DEFAULT NULL,
  `company_intro` TEXT,
  `job_title` VARCHAR(100) NOT NULL,
  `job_type` VARCHAR(20) DEFAULT NULL,
  `salary` VARCHAR(50) DEFAULT NULL,
  `city` VARCHAR(50) DEFAULT NULL,
  `address` VARCHAR(255) DEFAULT NULL,
  `experience` VARCHAR(50) DEFAULT NULL,
  `education` VARCHAR(50) DEFAULT NULL,
  `job_desc` TEXT,
  `job_requirement` TEXT,
  `welfare` VARCHAR(500) DEFAULT NULL,
  `contact_name` VARCHAR(50) DEFAULT NULL,
  `contact_phone` VARCHAR(20) DEFAULT NULL,
  `view_count` INT NOT NULL DEFAULT 0,
  `apply_count` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `real_name` VARCHAR(50) NOT NULL,
  `gender` TINYINT DEFAULT NULL,
  `avatar` VARCHAR(255) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `city` VARCHAR(50) DEFAULT NULL,
  `education` VARCHAR(50) DEFAULT NULL,
  `experience` VARCHAR(100) DEFAULT NULL,
  `expected_salary` VARCHAR(50) DEFAULT NULL,
  `skills` VARCHAR(500) DEFAULT NULL,
  `introduction` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `job_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_job` (`user_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `job_id` BIGINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `cancel_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_job` (`user_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`id`, `user_name`, `pass_word`, `nick_name`, `email`, `phone`) VALUES
(1, 'demo', '123456', '示例用户', 'demo@example.com', '13800000000'),
(2, 'zhangsan', '123456', '张三', 'zhangsan@example.com', '13800000002'),
(3, 'lisi', '123456', '李四', 'lisi@example.com', '13800000003'),
(4, 'wangwu', '123456', '王五', 'wangwu@example.com', '13800000004'),
(5, 'zhaoliu', '123456', '赵六', 'zhaoliu@example.com', '13800000005'),
(6, 'sunqi', '123456', '孙七', 'sunqi@example.com', '13800000006'),
(7, 'zhouba', '123456', '周八', 'zhouba@example.com', '13800000007'),
(8, 'wujiu', '123456', '吴九', 'wujiu@example.com', '13800000008');

INSERT INTO `job` (`id`, `company_name`, `company_logo`, `company_intro`, `job_title`, `job_type`, `salary`, `city`, `address`, `experience`, `education`, `job_desc`, `job_requirement`, `welfare`, `contact_name`, `contact_phone`, `view_count`, `apply_count`) VALUES
(1, '阿里巴巴', '/logo/alibaba.png', '全球领先的互联网公司，业务包括电商、云计算、数字媒体等', 'Java开发工程师', '全职', '15K-30K', '杭州', '余杭区文一西路969号', '3-5年', '本科', '负责后端服务开发与维护，熟悉Spring Boot。', '有Java开发经验，理解分布式系统。', '五险一金,年终奖,免费三餐', 'HR李女士', '0571-88158000', 0, 0),
(2, '腾讯科技', '/logo/tencent.png', '中国最大的互联网综合服务提供商，业务涵盖社交、游戏、金融等', '前端开发工程师', '全职', '18K-30K', '深圳', '南山区科技园', '1-3年', '本科', '负责前端页面开发，熟悉Vue/React。', '掌握HTML/CSS/JavaScript，了解前端工程化。', '五险一金,年终奖,免费班车', 'HR王先生', '0755-86013388', 0, 0),
(3, '字节跳动', '/logo/bytedance.png', '全球领先的移动互联网公司，旗下有抖音、今日头条等产品', 'Go开发工程师', '全职', '20K-40K', '北京', '海淀区知春路', '3-5年', '本科', '负责后端服务开发，熟悉Go语言与微服务。', '有高并发系统经验，熟悉常见中间件。', '五险一金,年终奖,免费三餐', 'HR张女士', '010-12345678', 0, 0);
