package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.ArticleVO;
import com.blog.service.LikeCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点赞收藏控制器
 * 处理文章的点赞和收藏请求
 */
@RestController
@RequestMapping("/api")
public class LikeCollectController {

    @Autowired
    private LikeCollectService likeCollectService;

    /**
     * 切换点赞状态（需要登录）
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     * @return 是否点赞
     */
    @PostMapping("/like-collect/like/{articleId}")
    public ApiResponse<Map<String, Boolean>> toggleLike(@PathVariable Long articleId,
                                                        @RequestAttribute("userId") Long userId) {
        boolean isLiked = likeCollectService.toggleLike(articleId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isLiked", isLiked);
        return ApiResponse.success(isLiked ? "点赞成功" : "取消点赞成功", result);
    }

    /**
     * 切换收藏状态（需要登录）
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     * @return 是否收藏
     */
    @PostMapping("/like-collect/collect/{articleId}")
    public ApiResponse<Map<String, Boolean>> toggleCollect(@PathVariable Long articleId,
                                                           @RequestAttribute("userId") Long userId) {
        boolean isCollected = likeCollectService.toggleCollect(articleId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("isCollected", isCollected);
        return ApiResponse.success(isCollected ? "收藏成功" : "取消收藏成功", result);
    }

    /**
     * 获取用户点赞的文章列表（需要登录）
     *
     * @param userId 当前用户ID
     * @return 文章列表
     */
    @GetMapping("/like-collect/liked-articles")
    public ApiResponse<List<ArticleVO>> getLikedArticles(@RequestAttribute("userId") Long userId) {
        List<ArticleVO> articles = likeCollectService.getLikedArticles(userId);
        return ApiResponse.success(articles);
    }

    /**
     * 获取用户收藏的文章列表（需要登录）
     *
     * @param userId 当前用户ID
     * @return 文章列表
     */
    @GetMapping("/like-collect/collected-articles")
    public ApiResponse<List<ArticleVO>> getCollectedArticles(@RequestAttribute("userId") Long userId) {
        List<ArticleVO> articles = likeCollectService.getCollectedArticles(userId);
        return ApiResponse.success(articles);
    }

    /**
     * 点赞文章（需要登录）
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     * @return 成功响应
     */
    @PostMapping("/articles/{articleId}/like")
    public ApiResponse<Void> likeArticle(@PathVariable Long articleId,
                                         @RequestAttribute("userId") Long userId) {
        likeCollectService.likeArticle(articleId, userId);
        return ApiResponse.success("点赞成功", null);
    }

    /**
     * 取消点赞（需要登录）
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     * @return 成功响应
     */
    @DeleteMapping("/articles/{articleId}/like")
    public ApiResponse<Void> unlikeArticle(@PathVariable Long articleId,
                                           @RequestAttribute("userId") Long userId) {
        likeCollectService.unlikeArticle(articleId, userId);
        return ApiResponse.success("取消点赞成功", null);
    }

    /**
     * 收藏文章（需要登录）
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     * @return 成功响应
     */
    @PostMapping("/articles/{articleId}/collect")
    public ApiResponse<Void> collectArticle(@PathVariable Long articleId,
                                            @RequestAttribute("userId") Long userId) {
        likeCollectService.collectArticle(articleId, userId);
        return ApiResponse.success("收藏成功", null);
    }

    /**
     * 取消收藏（需要登录）
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID
     * @return 成功响应
     */
    @DeleteMapping("/articles/{articleId}/collect")
    public ApiResponse<Void> uncollectArticle(@PathVariable Long articleId,
                                              @RequestAttribute("userId") Long userId) {
        likeCollectService.uncollectArticle(articleId, userId);
        return ApiResponse.success("取消收藏成功", null);
    }
}
