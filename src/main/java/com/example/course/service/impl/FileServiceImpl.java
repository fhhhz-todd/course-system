package com.example.course.service.impl;

import com.example.course.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileServiceImpl implements FileService {

    private static final String UPLOAD_DIR = "uploads/";

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一的文件名
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf('.')) : "";
        String newFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "_" + System.currentTimeMillis() + extension;
        
        // 保存文件
        Path filePath = Paths.get(UPLOAD_DIR, newFileName);
        Files.copy(file.getInputStream(), filePath); // 使用getInputStream()方法
        
        return newFileName;
    }

    @Override
    public String getFilePath(String fileName) {
        return UPLOAD_DIR + fileName;
    }
}