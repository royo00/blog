package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 发表评论
     *
     * @param commentDTO 评论信息
     * @param userId     用户ID
     * @return 评论ID
     */
    Long createComment(CommentDTO commentDTO, Long userId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 分页查询文章的评论列表
     *
     * @param articleId 文章ID
     * @param page      页码
     * @param pageSize  每页数量
     * @return 评论分页列表
     */
    Page<CommentVO> getCommentsByArticleId(Long articleId, Integer page, Integer pageSize);
}
