package com.recruitment.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏，userId + jobId
 */
@Data
@TableName("favorite")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;                      // 收藏ID
    private Long userId;                  // 用户ID
    private Long jobId;                   // 职位ID
    private LocalDateTime createTime;     // 收藏时间
}
