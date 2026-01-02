package com.blog.service;

import com.blog.entity.AccessLog;
import com.blog.entity.Article;
import com.blog.mapper.AccessLogMapper;
import com.blog.mapper.ArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 访问日志服务类
 */
@Slf4j
@Service
public class AccessLogService {

    @Autowired
    private AccessLogMapper accessLogMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 记录访问日志并增加文章浏览量
     *
     * @param articleId 文章ID
     * @param userId    用户ID（可为null）
     * @param request   HTTP请求对象
     */
    public void logAccess(Long articleId, Long userId, HttpServletRequest request) {
        try {
            // 1. 记录访问日志
            AccessLog accessLog = new AccessLog();
            accessLog.setArticleId(articleId);
            accessLog.setUserId(userId);
            accessLog.setIpAddress(getClientIpAddress(request));
            accessLog.setUserAgent(request.getHeader("User-Agent"));

            accessLogMapper.insert(accessLog);
            log.debug("记录访问日志成功 - 文章ID: {}, IP: {}", articleId, accessLog.getIpAddress());

            // 2. 增加文章浏览量
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                article.setViewCount(article.getViewCount() + 1);
                articleMapper.updateById(article);
                log.debug("增加文章浏览量成功 - 文章ID: {}, 当前浏览量: {}", articleId, article.getViewCount());
            }
        } catch (Exception e) {
            log.error("记录访问日志失败 - 文章ID: {}", articleId, e);
            // 不抛出异常，避免影响正常业务
        }
    }

    /**
     * 获取客户端真实IP地址
     * 考虑了代理和负载均衡的情况
     *
     * @param request HTTP请求对象
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于多级代理，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
