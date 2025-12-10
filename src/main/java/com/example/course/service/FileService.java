package com.example.course.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    /**
     * 保存上传的文件
     * @param file 上传的文件
     * @return 保存后的文件名
     * @throws IOException 文件操作异常
     */
    String saveFile(MultipartFile file) throws IOException;

    /**
     * 获取文件的完整路径
     * @param fileName 文件名
     * @return 文件完整路径
     */
    String getFilePath(String fileName);
}