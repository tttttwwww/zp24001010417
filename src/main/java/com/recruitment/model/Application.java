package com.recruitment.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递，userId + jobId + 状态
 */
@Data
public class Application {
    public static final int STATUS_APPLIED = 0;    // 已投递
    public static final int STATUS_CANCELED = 1;   // 已取消

    private Long id;                      // 投递ID
    private Long userId;                  // 用户ID
    private Long jobId;                   // 职位ID
    private int status;                   // 状态：0 已投递，1 已取消
    private LocalDateTime applyTime;      // 投递时间
    private LocalDateTime cancelTime;     // 取消时间
}
