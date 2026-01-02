package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.service.AccessLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 访问日志控制器
 * 处理访问记录相关请求
 */
@RestController
@RequestMapping("/api/access-log")
public class AccessLogController {

    @Autowired
    private AccessLogService accessLogService;

    /**
     * 记录文章访问
     *
     * @param articleId 文章ID
     * @param userId    当前用户ID（可选）
     * @param request   HTTP请求对象
     * @return 成功响应
     */
    @PostMapping("/article/{articleId}")
    public ApiResponse<Void> logArticleAccess(@PathVariable Long articleId,
                                              @RequestAttribute(value = "userId", required = false) Long userId,
                                              HttpServletRequest request) {
        accessLogService.logAccess(articleId, userId, request);
        return ApiResponse.success();
    }
}
