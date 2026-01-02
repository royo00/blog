package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 处理文件上传请求
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件访问URL
     */
    @PostMapping
    public ApiResponse<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadFile(file);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        return ApiResponse.success("上传成功", data);
    }
}
