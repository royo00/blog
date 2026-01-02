package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ApiResponse;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 * 处理文章的创建、查询、更新、删除等请求
 */
@Tag(name = "文章管理", description = "文章的增删改查、浏览量统计等接口")
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 创建文章（需要登录）
     *
     * @param articleDTO 文章信息
     * @param userId     当前用户ID
     * @return 文章ID
     */
    @Operation(summary = "创建文章", description = "发布新文章（需要管理员权限）",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ApiResponse<Long> createArticle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "文章信息", required = true)
            @RequestBody ArticleDTO articleDTO,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        Long articleId = articleService.createArticle(articleDTO, userId);
        return ApiResponse.success("发布成功", articleId);
    }

    /**
     * 更新文章（需要登录）
     *
     * @param id         文章ID
     * @param articleDTO 文章信息
     * @param userId     当前用户ID
     * @return 成功响应
     */
    @Operation(summary = "更新文章", description = "修改文章内容（需要管理员权限）",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    public ApiResponse<Void> updateArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "文章信息", required = true)
            @RequestBody ArticleDTO articleDTO,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        articleService.updateArticle(id, articleDTO, userId);
        return ApiResponse.success("更新成功", null);
    }

    /**
     * 删除文章（需要登录）
     *
     * @param id     文章ID
     * @param userId 当前用户ID
     * @return 成功响应
     */
    @Operation(summary = "删除文章", description = "删除指定文章（需要管理员权限）",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        articleService.deleteArticle(id, userId);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取文章详情（公开接口）
     *
     * @param id     文章ID
     * @param userId 当前用户ID（可选）
     * @return 文章详情
     */
    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息")
    @GetMapping("/{id}")
    public ApiResponse<ArticleVO> getArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId) {
        ArticleVO article = articleService.getArticleById(id, userId);
        return ApiResponse.success(article);
    }

    /**
     * 增加文章浏览量（公开接口）
     *
     * @param id 文章ID
     * @return 成功响应
     */
    @Operation(summary = "增加浏览量", description = "增加文章浏览计数")
    @PostMapping("/{id}/view")
    public ApiResponse<Void> incrementViewCount(
            @Parameter(description = "文章ID", required = true) @PathVariable Long id) {
        articleService.incrementViewCount(id);
        return ApiResponse.success();
    }

    /**
     * 分页查询文章列表（公开接口）
     *
     * @param page     页码（默认1）
     * @param pageSize 每页数量（默认10）
     * @param keyword  搜索关键词（可选）
     * @param tag      标签（可选）
     * @return 文章分页列表
     */
    @Operation(summary = "分页查询文章列表", description = "获取文章列表，支持关键词搜索和标签筛选")
    @GetMapping
    public ApiResponse<Page<ArticleVO>> getArticlePage(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "标签") @RequestParam(required = false) String tag) {
        Page<ArticleVO> articlePage = articleService.getArticlePage(page, pageSize, keyword, tag);
        return ApiResponse.success(articlePage);
    }

    /**
     * 搜索文章（公开接口）
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param pageSize 每页数量
     * @return 文章分页列表
     */
    @Operation(summary = "搜索文章", description = "根据关键词搜索文章")
    @GetMapping("/search")
    public ApiResponse<Page<ArticleVO>> searchArticles(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<ArticleVO> articlePage = articleService.getArticlePage(page, pageSize, keyword, null);
        return ApiResponse.success(articlePage);
    }
}
