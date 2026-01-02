package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论VO（用于返回给前端）
 */
@Data
public class CommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者信息
     */
    private UserDTO author;

    /**
     * 评论者是否被禁言
     */
    private Boolean isBanned;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
