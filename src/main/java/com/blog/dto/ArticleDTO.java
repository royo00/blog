package com.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章创建/编辑DTO
 */
@Data
public class ArticleDTO {

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 标签列表
     */
    private List<String> tags;
}
