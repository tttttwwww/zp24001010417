package com.recruitment.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏，userId + jobId
 */
@Data
public class Favorite {
    private Long id;                      // 收藏ID
    private Long userId;                  // 用户ID
    private Long jobId;                   // 职位ID
    private LocalDateTime createTime;     // 收藏时间
}
