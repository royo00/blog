package com.blog.service;

import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.dto.UserDTO;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegisterDTO registerDTO;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("password123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setNickname("测试用户");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setEmail("test@example.com");
        mockUser.setNickname("测试用户");
        mockUser.setRole("USER");
        mockUser.setIsBanned(false);
    }

    @Test
    void testRegister_Success() {
        // 模拟用户名不存在
        when(userMapper.selectByUsername(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行注册
        UserDTO result = userService.register(registerDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        // 验证方法调用
        verify(userMapper, times(1)).selectByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        // 模拟用户名已存在
        when(userMapper.selectByUsername(anyString())).thenReturn(mockUser);

        // 执行注册并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.register(registerDTO);
        });

        // 验证没有调用insert方法
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        // 模拟用户存在且密码正确
        when(userMapper.selectByUsername(anyString())).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // 执行登录
        String token = userService.login(loginDTO);

        // 验证结果
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 验证方法调用
        verify(userMapper, times(1)).selectByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    @Test
    void testLogin_UserNotFound() {
        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("nonexistent");
        loginDTO.setPassword("password123");

        // 模拟用户不存在
        when(userMapper.selectByUsername(anyString())).thenReturn(null);

        // 执行登录并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
    }

    @Test
    void testLogin_WrongPassword() {
        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");

        // 模拟用户存在但密码错误
        when(userMapper.selectByUsername(anyString())).thenReturn(mockUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // 执行登录并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
    }

    @Test
    void testLogin_UserBanned() {
        // 准备登录数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        // 模拟用户被禁言
        mockUser.setIsBanned(true);
        when(userMapper.selectByUsername(anyString())).thenReturn(mockUser);

        // 执行登录并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
    }

    @Test
    void testGetUserById_Success() {
        // 模拟查询成功
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        // 执行查询
        UserDTO result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());

        // 验证方法调用
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // 模拟用户不存在
        when(userMapper.selectById(anyLong())).thenReturn(null);

        // 执行查询并验证异常
        assertThrows(BusinessException.class, () -> {
            userService.getUserById(999L);
        });
    }
}
