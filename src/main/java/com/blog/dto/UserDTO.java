package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO（用于返回给前端）
 */
@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 是否管理员
     */
    private Boolean isAdmin;

    /**
     * 用户角色（ADMIN或USER）
     */
    private String role;

    /**
     * 是否禁言
     */
    private Boolean isBanned;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
