package com.blog.service;

import com.blog.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 根据名称获取或创建标签
     *
     * @param tagName 标签名称
     * @return 标签实体
     */
    Tag getOrCreateTag(String tagName);

    /**
     * 为文章绑定标签
     *
     * @param articleId 文章ID
     * @param tagNames  标签名称列表
     */
    void bindTagsToArticle(Long articleId, List<String> tagNames);

    /**
     * 获取文章的标签列表
     *
     * @param articleId 文章ID
     * @return 标签名称列表
     */
    List<String> getArticleTags(Long articleId);

    /**
     * 删除文章的所有标签关联
     *
     * @param articleId 文章ID
     */
    void removeArticleTags(Long articleId);
}
