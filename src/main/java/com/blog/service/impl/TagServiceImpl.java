package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public Tag getOrCreateTag(String tagName) {
        // 查询标签是否存在
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tagName);
        Tag tag = tagMapper.selectOne(wrapper);

        // 不存在则创建
        if (tag == null) {
            tag = new Tag();
            tag.setName(tagName);
            tag.setCount(0);
            tagMapper.insert(tag);
        }

        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTagsToArticle(Long articleId, List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return;
        }

        for (String tagName : tagNames) {
            // 获取或创建标签
            Tag tag = getOrCreateTag(tagName);

            // 创建文章-标签关联
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tag.getId());
            articleTagMapper.insert(articleTag);

            // 更新标签计数
            tag.setCount(tag.getCount() + 1);
            tagMapper.updateById(tag);
        }
    }

    @Override
    public List<String> getArticleTags(Long articleId) {
        // 查询文章的所有标签ID
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(articleTags)) {
            return new ArrayList<>();
        }

        // 查询标签详情
        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());

        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeArticleTags(Long articleId) {
        // 查询文章的所有标签
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(articleTags)) {
            return;
        }

        // 减少标签计数
        for (ArticleTag articleTag : articleTags) {
            Tag tag = tagMapper.selectById(articleTag.getTagId());
            if (tag != null && tag.getCount() > 0) {
                tag.setCount(tag.getCount() - 1);
                tagMapper.updateById(tag);
            }
        }

        // 删除文章-标签关联
        articleTagMapper.delete(wrapper);
    }
}
