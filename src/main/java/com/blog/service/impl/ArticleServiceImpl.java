package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.dto.UserDTO;
import com.blog.entity.Article;
import com.blog.entity.UserCollect;
import com.blog.entity.UserLike;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserCollectMapper;
import com.blog.mapper.UserLikeMapper;
import com.blog.service.ArticleService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserLikeMapper userLikeMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleDTO articleDTO, Long userId) {
        // 1. 参数校验
        if (!StringUtils.hasText(articleDTO.getTitle())) {
            throw new BusinessException("文章标题不能为空");
        }
        if (!StringUtils.hasText(articleDTO.getContent())) {
            throw new BusinessException("文章内容不能为空");
        }

        // 2. 创建文章
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setSummary(articleDTO.getSummary());
        article.setCoverImage(articleDTO.getCoverImage());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setCommentCount(0);
        article.setIsDeleted(0);

        // 3. 保存文章
        int result = articleMapper.insert(article);
        if (result == 0) {
            throw new BusinessException("创建文章失败");
        }

        // 4. 绑定标签
        if (articleDTO.getTags() != null && !articleDTO.getTags().isEmpty()) {
            tagService.bindTagsToArticle(article.getId(), articleDTO.getTags());
        }

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long articleId, ArticleDTO articleDTO, Long userId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 2. 权限校验
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权修改该文章");
        }

        // 3. 更新文章
        if (StringUtils.hasText(articleDTO.getTitle())) {
            article.setTitle(articleDTO.getTitle());
        }
        if (StringUtils.hasText(articleDTO.getContent())) {
            article.setContent(articleDTO.getContent());
        }
        article.setSummary(articleDTO.getSummary());
        article.setCoverImage(articleDTO.getCoverImage());

        int result = articleMapper.updateById(article);
        if (result == 0) {
            throw new BusinessException("更新文章失败");
        }

        // 4. 更新标签
        if (articleDTO.getTags() != null) {
            tagService.removeArticleTags(articleId);
            if (!articleDTO.getTags().isEmpty()) {
                tagService.bindTagsToArticle(articleId, articleDTO.getTags());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId, Long userId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 2. 权限校验
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该文章");
        }

        // 3. 逻辑删除文章
        article.setIsDeleted(1);
        int result = articleMapper.updateById(article);
        if (result == 0) {
            throw new BusinessException("删除文章失败");
        }

        // 4. 删除标签关联
        tagService.removeArticleTags(articleId);
    }

    @Override
    public ArticleVO getArticleById(Long articleId, Long userId) {
        // 1. 查询文章
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }

        // 2. 转换为VO
        return convertToVO(article, userId);
    }

    @Override
    public Page<ArticleVO> getArticlePage(Integer page, Integer pageSize, String keyword, String tag) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsDeleted, 0);

        // 搜索关键词
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getContent, keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreatedAt);

        // 2. 分页查询
        Page<Article> articlePage = new Page<>(page, pageSize);
        articlePage = articleMapper.selectPage(articlePage, wrapper);

        // 3. 转换为VO
        Page<ArticleVO> voPage = new Page<>(page, pageSize);
        voPage.setTotal(articlePage.getTotal());
        List<ArticleVO> voList = articlePage.getRecords().stream()
                .map(article -> convertToVO(article, null))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public void incrementViewCount(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            articleMapper.updateById(article);
        }
    }

    /**
     * 将Article转换为ArticleVO
     *
     * @param article 文章实体
     * @param userId  当前用户ID（可为null）
     * @return 文章VO
     */
    private ArticleVO convertToVO(Article article, Long userId) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);

        // 设置作者信息
        UserDTO author = userService.getUserById(article.getUserId());
        vo.setAuthor(author);

        // 设置标签
        List<String> tags = tagService.getArticleTags(article.getId());
        vo.setTags(tags);

        // 设置点赞和收藏状态
        if (userId != null) {
            vo.setIsLiked(checkUserLiked(article.getId(), userId));
            vo.setIsCollected(checkUserCollected(article.getId(), userId));
        } else {
            vo.setIsLiked(false);
            vo.setIsCollected(false);
        }

        return vo;
    }

    /**
     * 检查用户是否点赞了文章
     */
    private Boolean checkUserLiked(Long articleId, Long userId) {
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getArticleId, articleId)
                .eq(UserLike::getUserId, userId);
        return userLikeMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否收藏了文章
     */
    private Boolean checkUserCollected(Long articleId, Long userId) {
        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollect::getArticleId, articleId)
                .eq(UserCollect::getUserId, userId);
        return userCollectMapper.selectCount(wrapper) > 0;
    }
}
