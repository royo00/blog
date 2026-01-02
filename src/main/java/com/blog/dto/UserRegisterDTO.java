package com.blog.dto;

import lombok.Data;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;
}
