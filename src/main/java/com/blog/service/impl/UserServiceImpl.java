package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.UserDTO;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import com.blog.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserDTO register(UserRegisterDTO registerDTO) {
        // 1. 参数校验
        if (!StringUtils.hasText(registerDTO.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(registerDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (registerDTO.getUsername().length() < 3 || registerDTO.getUsername().length() > 20) {
            throw new BusinessException("用户名长度必须在3-20个字符之间");
        }

        // 2. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setNickname(StringUtils.hasText(registerDTO.getNickname())
                ? registerDTO.getNickname()
                : registerDTO.getUsername());
        user.setIsAdmin(0);
        user.setIsBanned(0);
        user.setIsDeleted(0);

        // 4. 保存到数据库
        int result = userMapper.insert(user);
        if (result == 0) {
            throw new BusinessException("注册失败");
        }

        // 5. 返回用户信息
        return convertToDTO(user);
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        // 1. 参数校验
        if (!StringUtils.hasText(loginDTO.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 2. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getIsBanned() == 1) {
            throw new BusinessException("账号已被禁言");
        }
        if (user.getIsDeleted() == 1) {
            throw new BusinessException("账号已被删除");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 5. 生成JWT Token
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getIsAdmin() == 1);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToDTO(user);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void banUser(Long userId, Boolean isBanned) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setIsBanned(isBanned ? 1 : 0);
        int result = userMapper.updateById(user);
        if (result == 0) {
            throw new BusinessException("操作失败");
        }
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setIsDeleted(1);
        user.setDeletedAt(LocalDateTime.now());
        int result = userMapper.updateById(user);
        if (result == 0) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setIsAdmin(user.getIsAdmin() == 1);
        dto.setIsBanned(user.getIsBanned() == 1);
        dto.setRole(user.getIsAdmin() == 1 ? "ADMIN" : "USER");
        return dto;
    }
}
