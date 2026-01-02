package com.blog.service;

import com.blog.dto.ArticleVO;
import java.util.List;

/**
 * 点赞收藏服务接口
 */
public interface LikeCollectService {

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    void likeArticle(Long articleId, Long userId);

    /**
     * 取消点赞
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    void unlikeArticle(Long articleId, Long userId);

    /**
     * 收藏文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    void collectArticle(Long articleId, Long userId);

    /**
     * 取消收藏
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    void uncollectArticle(Long articleId, Long userId);

    /**
     * 切换点赞状态
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 是否点赞
     */
    boolean toggleLike(Long articleId, Long userId);

    /**
     * 切换收藏状态
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 是否收藏
     */
    boolean toggleCollect(Long articleId, Long userId);

    /**
     * 获取用户点赞的文章列表
     *
     * @param userId 用户ID
     * @return 文章列表
     */
    List<ArticleVO> getLikedArticles(Long userId);

    /**
     * 获取用户收藏的文章列表
     *
     * @param userId 用户ID
     * @return 文章列表
     */
    List<ArticleVO> getCollectedArticles(Long userId);
}
