package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ArticleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TagService tagService;

    @InjectMocks
    private ArticleService articleService;

    private ArticleDTO articleDTO;
    private Article mockArticle;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 准备文章DTO
        articleDTO = new ArticleDTO();
        articleDTO.setTitle("测试文章标题");
        articleDTO.setContent("这是测试文章的内容");
        articleDTO.setSummary("文章摘要");
        articleDTO.setTags(Arrays.asList("Java", "Spring Boot"));

        // 准备Mock文章
        mockArticle = new Article();
        mockArticle.setId(1L);
        mockArticle.setTitle("测试文章标题");
        mockArticle.setContent("这是测试文章的内容");
        mockArticle.setSummary("文章摘要");
        mockArticle.setAuthorId(1L);
        mockArticle.setViewCount(0);
        mockArticle.setLikeCount(0);
        mockArticle.setCollectCount(0);
        mockArticle.setCommentCount(0);

        // 准备Mock用户
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setNickname("测试用户");
        mockUser.setRole("ADMIN");
    }

    @Test
    void testCreateArticle_Success() {
        // 模拟用户是管理员
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(articleMapper.insert(any(Article.class))).thenAnswer(invocation -> {
            Article article = invocation.getArgument(0);
            article.setId(1L);
            return 1;
        });

        // 执行创建
        Long articleId = articleService.createArticle(articleDTO, 1L);

        // 验证结果
        assertNotNull(articleId);
        assertEquals(1L, articleId);

        // 验证方法调用
        verify(userMapper, times(1)).selectById(1L);
        verify(articleMapper, times(1)).insert(any(Article.class));
    }

    @Test
    void testCreateArticle_NotAdmin() {
        // 模拟用户不是管理员
        mockUser.setRole("USER");
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        // 执行创建并验证异常
        assertThrows(BusinessException.class, () -> {
            articleService.createArticle(articleDTO, 1L);
        });

        // 验证没有插入文章
        verify(articleMapper, never()).insert(any(Article.class));
    }

    @Test
    void testGetArticleById_Success() {
        // 模拟查询成功
        when(articleMapper.selectById(1L)).thenReturn(mockArticle);
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        // 执行查询
        ArticleVO result = articleService.getArticleById(1L, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试文章标题", result.getTitle());
        assertEquals("测试用户", result.getAuthorName());

        // 验证方法调用
        verify(articleMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetArticleById_NotFound() {
        // 模拟文章不存在
        when(articleMapper.selectById(anyLong())).thenReturn(null);

        // 执行查询并验证异常
        assertThrows(BusinessException.class, () -> {
            articleService.getArticleById(999L, null);
        });
    }

    @Test
    void testUpdateArticle_Success() {
        // 模拟文章存在且用户是作者
        when(articleMapper.selectById(1L)).thenReturn(mockArticle);
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        // 执行更新
        assertDoesNotThrow(() -> {
            articleService.updateArticle(1L, articleDTO, 1L);
        });

        // 验证方法调用
        verify(articleMapper, times(1)).selectById(1L);
        verify(articleMapper, times(1)).updateById(any(Article.class));
    }

    @Test
    void testUpdateArticle_NotAuthor() {
        // 模拟文章存在但用户不是作者
        mockArticle.setAuthorId(2L);
        mockUser.setRole("USER");
        when(articleMapper.selectById(1L)).thenReturn(mockArticle);
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        // 执行更新并验证异常
        assertThrows(BusinessException.class, () -> {
            articleService.updateArticle(1L, articleDTO, 1L);
        });

        // 验证没有更新
        verify(articleMapper, never()).updateById(any(Article.class));
    }

    @Test
    void testDeleteArticle_Success() {
        // 模拟文章存在且用户是管理员
        when(articleMapper.selectById(1L)).thenReturn(mockArticle);
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(articleMapper.deleteById(1L)).thenReturn(1);

        // 执行删除
        assertDoesNotThrow(() -> {
            articleService.deleteArticle(1L, 1L);
        });

        // 验证方法调用
        verify(articleMapper, times(1)).selectById(1L);
        verify(articleMapper, times(1)).deleteById(1L);
    }

    @Test
    void testIncrementViewCount_Success() {
        // 模拟文章存在
        when(articleMapper.selectById(1L)).thenReturn(mockArticle);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        // 执行增加浏览量
        assertDoesNotThrow(() -> {
            articleService.incrementViewCount(1L);
        });

        // 验证浏览量增加
        verify(articleMapper, times(1)).selectById(1L);
        verify(articleMapper, times(1)).updateById(any(Article.class));
    }
}
