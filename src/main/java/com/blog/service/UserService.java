package com.blog.service;

import com.blog.dto.UserDTO;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserDTO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return JWT Token
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 禁言用户
     *
     * @param userId   用户ID
     * @param isBanned 是否禁言
     */
    void banUser(Long userId, Boolean isBanned);

    /**
     * 逻辑删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 将User实体转换为UserDTO
     *
     * @param user 用户实体
     * @return 用户DTO
     */
    UserDTO convertToDTO(User user);
}
