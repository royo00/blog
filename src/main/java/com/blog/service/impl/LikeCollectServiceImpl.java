package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.ArticleVO;
import com.blog.dto.UserDTO;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.entity.UserCollect;
import com.blog.entity.UserLike;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserCollectMapper;
import com.blog.mapper.UserLikeMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.LikeCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 点赞收藏服务实现类
 */
@Service
public class LikeCollectServiceImpl implements LikeCollectService {

    @Autowired
    private UserLikeMapper userLikeMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId, Long userId) {
        // 1. 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }

        // 2. 检查是否已点赞
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getArticleId, articleId)
                .eq(UserLike::getUserId, userId);
        UserLike existLike = userLikeMapper.selectOne(wrapper);
        if (existLike != null) {
            throw new BusinessException("您已点赞过该文章");
        }

        // 3. 创建点赞记录
        UserLike userLike = new UserLike();
        userLike.setArticleId(articleId);
        userLike.setUserId(userId);
        userLikeMapper.insert(userLike);

        // 4. 更新文章点赞计数
        article.setLikeCount(article.getLikeCount() + 1);
        articleMapper.updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId, Long userId) {
        // 1. 查询点赞记录
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getArticleId, articleId)
                .eq(UserLike::getUserId, userId);
        UserLike userLike = userLikeMapper.selectOne(wrapper);
        if (userLike == null) {
            throw new BusinessException("您未点赞该文章");
        }

        // 2. 删除点赞记录
        userLikeMapper.deleteById(userLike.getId());

        // 3. 更新文章点赞计数
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getLikeCount() > 0) {
            article.setLikeCount(article.getLikeCount() - 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId, Long userId) {
        // 1. 检查文章是否存在
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getIsDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }

        // 2. 检查是否已收藏
        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollect::getArticleId, articleId)
                .eq(UserCollect::getUserId, userId);
        UserCollect existCollect = userCollectMapper.selectOne(wrapper);
        if (existCollect != null) {
            throw new BusinessException("您已收藏过该文章");
        }

        // 3. 创建收藏记录
        UserCollect userCollect = new UserCollect();
        userCollect.setArticleId(articleId);
        userCollect.setUserId(userId);
        userCollectMapper.insert(userCollect);

        // 4. 更新文章收藏计数
        article.setCollectCount(article.getCollectCount() + 1);
        articleMapper.updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId, Long userId) {
        // 1. 查询收藏记录
        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollect::getArticleId, articleId)
                .eq(UserCollect::getUserId, userId);
        UserCollect userCollect = userCollectMapper.selectOne(wrapper);
        if (userCollect == null) {
            throw new BusinessException("您未收藏该文章");
        }

        // 2. 删除收藏记录
        userCollectMapper.deleteById(userCollect.getId());

        // 3. 更新文章收藏计数
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getCollectCount() > 0) {
            article.setCollectCount(article.getCollectCount() - 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long articleId, Long userId) {
        // 检查是否已点赞
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getArticleId, articleId)
                .eq(UserLike::getUserId, userId);
        UserLike existLike = userLikeMapper.selectOne(wrapper);

        if (existLike != null) {
            // 已点赞，取消点赞
            userLikeMapper.deleteById(existLike.getId());
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getLikeCount() > 0) {
                article.setLikeCount(article.getLikeCount() - 1);
                articleMapper.updateById(article);
            }
            return false;
        } else {
            // 未点赞，添加点赞
            Article article = articleMapper.selectById(articleId);
            if (article == null || article.getIsDeleted() == 1) {
                throw new BusinessException("文章不存在");
            }
            UserLike userLike = new UserLike();
            userLike.setArticleId(articleId);
            userLike.setUserId(userId);
            userLikeMapper.insert(userLike);
            article.setLikeCount(article.getLikeCount() + 1);
            articleMapper.updateById(article);
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long articleId, Long userId) {
        // 检查是否已收藏
        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollect::getArticleId, articleId)
                .eq(UserCollect::getUserId, userId);
        UserCollect existCollect = userCollectMapper.selectOne(wrapper);

        if (existCollect != null) {
            // 已收藏，取消收藏
            userCollectMapper.deleteById(existCollect.getId());
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getCollectCount() > 0) {
                article.setCollectCount(article.getCollectCount() - 1);
                articleMapper.updateById(article);
            }
            return false;
        } else {
            // 未收藏，添加收藏
            Article article = articleMapper.selectById(articleId);
            if (article == null || article.getIsDeleted() == 1) {
                throw new BusinessException("文章不存在");
            }
            UserCollect userCollect = new UserCollect();
            userCollect.setArticleId(articleId);
            userCollect.setUserId(userId);
            userCollectMapper.insert(userCollect);
            article.setCollectCount(article.getCollectCount() + 1);
            articleMapper.updateById(article);
            return true;
        }
    }

    @Override
    public List<ArticleVO> getLikedArticles(Long userId) {
        // 获取用户点赞的文章ID列表
        LambdaQueryWrapper<UserLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserLike::getUserId, userId)
                .orderByDesc(UserLike::getCreatedAt);
        List<UserLike> likes = userLikeMapper.selectList(likeWrapper);

        List<ArticleVO> articles = new ArrayList<>();
        for (UserLike like : likes) {
            Article article = articleMapper.selectById(like.getArticleId());
            if (article != null && article.getIsDeleted() == 0) {
                articles.add(convertToVO(article));
            }
        }
        return articles;
    }

    @Override
    public List<ArticleVO> getCollectedArticles(Long userId) {
        // 获取用户收藏的文章ID列表
        LambdaQueryWrapper<UserCollect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(UserCollect::getUserId, userId)
                .orderByDesc(UserCollect::getCreatedAt);
        List<UserCollect> collects = userCollectMapper.selectList(collectWrapper);

        List<ArticleVO> articles = new ArrayList<>();
        for (UserCollect collect : collects) {
            Article article = articleMapper.selectById(collect.getArticleId());
            if (article != null && article.getIsDeleted() == 0) {
                articles.add(convertToVO(article));
            }
        }
        return articles;
    }

    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCollectCount(article.getCollectCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setCreatedAt(article.getCreatedAt());

        // 获取作者信息
        User author = userMapper.selectById(article.getUserId());
        if (author != null) {
            UserDTO authorDTO = new UserDTO();
            authorDTO.setId(author.getId());
            authorDTO.setUsername(author.getUsername());
            authorDTO.setNickname(author.getNickname());
            vo.setAuthor(authorDTO);
        }
        return vo;
    }
}
