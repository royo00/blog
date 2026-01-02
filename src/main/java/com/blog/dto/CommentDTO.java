package com.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 评论创建DTO
 */
@Data
public class CommentDTO {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * @提及的用户列表
     */
    private List<String> mentions;
}
