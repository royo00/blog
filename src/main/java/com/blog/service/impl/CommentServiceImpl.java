package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;
import com.blog.dto.UserDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentDTO commentDTO, Long userId) {
        // 1. 参数校验
        if (!StringUtils.hasText(commentDTO.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }

        // 2. 检查文章是否存在
        Article article = articleMapper.selectById(commentDTO.getArticleId());
        if (article == null || article.getIsDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }

        // 3. 检查用户是否被禁言
        User user = userService.getUserByUsername(userService.getUserById(userId).getUsername());
        if (user.getIsBanned() == 1) {
            throw new BusinessException("您已被禁言，无法发表评论");
        }

        // 4. 创建评论
        Comment comment = new Comment();
        comment.setArticleId(commentDTO.getArticleId());
        comment.setUserId(userId);
        comment.setContent(commentDTO.getContent());

        // 处理@提及
        if (commentDTO.getMentions() != null && !commentDTO.getMentions().isEmpty()) {
            try {
                String mentionsJson = objectMapper.writeValueAsString(commentDTO.getMentions());
                comment.setMentions(mentionsJson);
            } catch (JsonProcessingException e) {
                // 忽略JSON转换失败
            }
        }

        comment.setIsDeleted(0);

        // 5. 保存评论
        int result = commentMapper.insert(comment);
        if (result == 0) {
            throw new BusinessException("发表评论失败");
        }

        // 6. 更新文章评论计数
        article.setCommentCount(article.getCommentCount() + 1);
        articleMapper.updateById(article);

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 2. 权限校验（只能删除自己的评论，管理员除外）
        User user = userService.getUserByUsername(userService.getUserById(userId).getUsername());
        if (!comment.getUserId().equals(userId) && user.getIsAdmin() != 1) {
            throw new BusinessException("无权删除该评论");
        }

        // 3. 逻辑删除评论
        comment.setIsDeleted(1);
        int result = commentMapper.updateById(comment);
        if (result == 0) {
            throw new BusinessException("删除评论失败");
        }

        // 4. 更新文章评论计数
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null && article.getCommentCount() > 0) {
            article.setCommentCount(article.getCommentCount() - 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    public Page<CommentVO> getCommentsByArticleId(Long articleId, Integer page, Integer pageSize) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getIsDeleted, 0)
                .orderByDesc(Comment::getCreatedAt);

        // 2. 分页查询
        Page<Comment> commentPage = new Page<>(page, pageSize);
        commentPage = commentMapper.selectPage(commentPage, wrapper);

        // 3. 转换为VO
        Page<CommentVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(commentPage.getTotal());
        List<CommentVO> voList = commentPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 将Comment转换为CommentVO
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);

        // 设置作者信息
        if (comment.getUserId() != null) {
            UserDTO author = userService.getUserById(comment.getUserId());
            vo.setAuthor(author);
            vo.setIsBanned(author.getIsBanned());
        }

        return vo;
    }
}
