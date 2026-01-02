package com.blog.controller;

import com.blog.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于测试系统是否正常运行
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 系统状态信息
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "Blog系统运行正常");

        return ApiResponse.success(data);
    }
}
