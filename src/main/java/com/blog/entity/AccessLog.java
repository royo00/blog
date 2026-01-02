package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问日志实体类
 */
@Data
@TableName("access_log")
public class AccessLog {

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID（未登录为NULL）
     */
    private Long userId;

    /**
     * 访问者IP
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 访问时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
