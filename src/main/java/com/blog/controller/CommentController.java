package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ApiResponse;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;
import com.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 * 处理评论的发表、删除、查询等请求
 */
@Tag(name = "评论管理", description = "评论的发表、删除、查询等接口")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论（需要登录）
     *
     * @param commentDTO 评论信息
     * @param userId     当前用户ID
     * @return 评论ID
     */
    @Operation(summary = "发表评论", description = "对文章发表评论（需要登录）",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ApiResponse<Long> createComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "评论信息", required = true)
            @RequestBody CommentDTO commentDTO,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        Long commentId = commentService.createComment(commentDTO, userId);
        return ApiResponse.success("评论成功", commentId);
    }

    /**
     * 删除评论（需要登录）
     *
     * @param id     评论ID
     * @param userId 当前用户ID
     * @return 成功响应
     */
    @Operation(summary = "删除评论", description = "删除自己的评论或管理员删除任意评论",
            security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        commentService.deleteComment(id, userId);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 获取文章的评论列表（公开接口）
     *
     * @param articleId 文章ID
     * @param page      页码
     * @param pageSize  每页数量
     * @return 评论分页列表
     */
    @Operation(summary = "获取文章评论", description = "分页获取指定文章的评论列表")
    @GetMapping("/article/{articleId}")
    public ApiResponse<Page<CommentVO>> getCommentsByArticleId(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<CommentVO> commentPage = commentService.getCommentsByArticleId(articleId, page, pageSize);
        return ApiResponse.success(commentPage);
    }
}
