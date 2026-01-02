package com.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 视图控制器
 * 处理前端页面路由请求（返回Thymeleaf模板）
 */
@Controller
public class ViewController {

    /**
     * 首页 - 文章列表
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 文章详情页
     */
    @GetMapping("/article/{id}")
    public String articleDetail(@PathVariable Long id) {
        return "article-detail";
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 注册页
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * 用户个人中心
     */
    @GetMapping("/user/profile")
    public String userProfile() {
        return "user-profile";
    }

    /**
     * 管理后台 - 统计Dashboard
     */
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    /**
     * 管理后台 - 文章管理
     */
    @GetMapping("/admin/articles")
    public String adminArticles() {
        return "admin/articles";
    }

    /**
     * 管理后台 - 用户管理
     */
    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
    }

    /**
     * 管理后台 - 文章编辑器
     */
    @GetMapping("/admin/article/edit")
    public String adminArticleEdit() {
        return "admin/article-edit";
    }

    /**
     * 管理后台 - 文章编辑器（编辑指定文章）
     */
    @GetMapping("/admin/article/edit/{id}")
    public String adminArticleEditById(@PathVariable Long id) {
        return "admin/article-edit";
    }
}
