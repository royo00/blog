package com.blog.service.impl;

import com.blog.exception.BusinessException;
import com.blog.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 允许的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    /**
     * 允许的文档类型
     */
    private static final List<String> ALLOWED_DOC_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * 最大文件大小（10MB）
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Override
    public String uploadFile(MultipartFile file) {
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 2. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        // 3. 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null ||
                (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOC_TYPES.contains(contentType))) {
            throw new BusinessException("不支持的文件类型");
        }

        // 4. 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "-" + originalFilename;

        // 5. 构建文件路径（按日期分目录）
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String fileType = ALLOWED_IMAGE_TYPES.contains(contentType) ? "images" : "documents";
        String relativePath = fileType + "/" + datePath + "/" + newFilename;
        String fullPath = uploadPath + "/" + relativePath;

        // 6. 创建目录
        File directory = new File(uploadPath + "/" + fileType + "/" + datePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 7. 保存文件
        try {
            Path path = Paths.get(fullPath);
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // 8. 返回访问URL
        return "/" + uploadPath + "/" + relativePath;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
