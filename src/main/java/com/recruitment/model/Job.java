package com.recruitment.model;

import lombok.Builder;
import lombok.Data;

/**
 * 职位（公司信息先写在同一个类里）
 */
@Data
@Builder
public class Job {
    private Long id;                    // 职位ID
    private String companyName;         // 公司名称
    private String companyLogo;         // 公司Logo
    private String companyIntro;        // 公司简介
    private String jobTitle;            // 职位名称
    private String jobType;             // 职位类型（全职 / 实习等）
    private String salary;              // 薪资范围
    private String city;                // 工作城市
    private String address;             // 工作地址
    private String experience;          // 经验要求
    private String education;           // 学历要求
    private String jobDesc;             // 职位描述
    private String jobRequirement;      // 任职要求
    private String welfare;             // 福利待遇
    private String contactName;         // 联系人
    private String contactPhone;        // 联系电话
    private int viewCount;              // 浏览次数
    private int applyCount;             // 投递次数
}
