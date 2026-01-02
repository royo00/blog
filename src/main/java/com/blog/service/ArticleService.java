package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 创建文章
     *
     * @param articleDTO 文章信息
     * @param userId     作者ID
     * @return 文章ID
     */
    Long createArticle(ArticleDTO articleDTO, Long userId);

    /**
     * 更新文章
     *
     * @param articleId  文章ID
     * @param articleDTO 文章信息
     * @param userId     当前用户ID
     */
    void updateArticle(Long articleId, ArticleDTO articleDTO, Long userId);

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     */
    void deleteArticle(Long articleId, Long userId);

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID（可为null）
     * @return 文章详情
     */
    ArticleVO getArticleById(Long articleId, Long userId);

    /**
     * 分页查询文章列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键词（可选）
     * @param tag      标签（可选）
     * @return 文章分页列表
     */
    Page<ArticleVO> getArticlePage(Integer page, Integer pageSize, String keyword, String tag);

    /**
     * 增加文章浏览量
     *
     * @param articleId 文章ID
     */
    void incrementViewCount(Long articleId);
}
