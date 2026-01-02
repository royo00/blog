package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.UserDTO;
import com.blog.dto.UserLoginDTO;
import com.blog.dto.UserRegisterDTO;
import com.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户注册、登录等认证相关请求
 */
@Tag(name = "认证管理", description = "用户注册、登录、获取当前用户信息等接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名已存在或参数错误")
    })
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "注册信息", required = true)
            @RequestBody UserRegisterDTO registerDTO) {
        UserDTO userDTO = userService.register(registerDTO);
        return ApiResponse.success("注册成功", userDTO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return Token和用户信息
     */
    @Operation(summary = "用户登录", description = "用户登录获取JWT Token")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "登录信息", required = true)
            @RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);

        // 获取用户信息
        UserDTO user = userService.convertToDTO(
                userService.getUserByUsername(loginDTO.getUsername())
        );

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return ApiResponse.success("登录成功", data);
    }

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID（从请求属性中获取）
     * @return 用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "通过Token获取当前登录用户的详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录或Token无效")
    })
    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        UserDTO userDTO = userService.getUserById(userId);
        return ApiResponse.success(userDTO);
    }
}
