package com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.ApiResponse;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.SiteStatistics;
import com.blog.entity.User;
import com.blog.dto.UserDTO;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.StatisticsService;
import com.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 * 处理管理员相关请求（需要管理员权限）
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取仪表盘统计数据
     * 自动聚合最近7天的访问量数据
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 自动聚合最近7天的统计数据
        try {
            statisticsService.aggregateRecentStatistics(7);
        } catch (Exception e) {
            // 聚合失败不影响返回已有数据
        }

        // 总文章数
        LambdaQueryWrapper<Article> articleQuery = new LambdaQueryWrapper<>();
        articleQuery.eq(Article::getIsDeleted, 0);
        long totalArticles = articleMapper.selectCount(articleQuery);
        stats.put("totalArticles", totalArticles);

        // 总用户数
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getIsDeleted, 0);
        long totalUsers = userMapper.selectCount(userQuery);
        stats.put("totalUsers", totalUsers);

        // 总评论数
        LambdaQueryWrapper<Comment> commentQuery = new LambdaQueryWrapper<>();
        commentQuery.eq(Comment::getIsDeleted, 0);
        long totalComments = commentMapper.selectCount(commentQuery);
        stats.put("totalComments", totalComments);

        // 总浏览量（所有文章的浏览量总和）
        LambdaQueryWrapper<Article> viewQuery = new LambdaQueryWrapper<>();
        viewQuery.eq(Article::getIsDeleted, 0);
        List<Article> articles = articleMapper.selectList(viewQuery);
        long totalViews = articles.stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();
        stats.put("totalViews", totalViews);

        // 最近7天的统计数据（用于趋势图）
        List<SiteStatistics> recentStats = statisticsService.getRecentStatistics(7);
        stats.put("recentStatistics", recentStats);

        return ApiResponse.success(stats);
    }

    /**
     * 手动刷新统计数据
     *
     * @return 成功响应
     */
    @PostMapping("/statistics/refresh")
    public ApiResponse<Void> refreshStatistics() {
        statisticsService.aggregateRecentStatistics(7);
        return ApiResponse.success("统计数据刷新成功", null);
    }

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    @GetMapping("/users")
    public ApiResponse<List<UserDTO>> getAllUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDeleted, 0)
                .orderByDesc(User::getCreatedAt);
        List<User> users = userMapper.selectList(queryWrapper);

        List<UserDTO> userDTOs = users.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setNickname(user.getNickname());
            dto.setAvatarUrl(user.getAvatarUrl());
            boolean isAdmin = user.getIsAdmin() != null && user.getIsAdmin() == 1;
            dto.setIsAdmin(isAdmin);
            dto.setRole(isAdmin ? "ADMIN" : "USER");
            dto.setIsBanned(user.getIsBanned() != null && user.getIsBanned() == 1);
            dto.setCreatedAt(user.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        return ApiResponse.success(userDTOs);
    }

    /**
     * 禁言/解禁用户
     *
     * @param userId 用户ID
     * @param params 参数（包含isBanned）
     * @return 成功响应
     */
    @PutMapping("/users/{userId}/ban")
    public ApiResponse<Void> banUser(@PathVariable Long userId,
                                     @RequestBody Map<String, Boolean> params) {
        Boolean isBanned = params.get("isBanned");
        userService.banUser(userId, isBanned);
        return ApiResponse.success(isBanned ? "禁言成功" : "解禁成功", null);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 成功响应
     */
    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.success("删除用户成功", null);
    }
}
