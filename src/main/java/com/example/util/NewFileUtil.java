package com.example.util;

import jakarta.servlet.ServletOutputStream;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;


//import javax.servlet.ServletOutputStream;
import java.io.*;
import java.net.URLEncoder;
import java.util.Objects;

@Component
public class NewFileUtil {
    public File MultipartFileToFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        // 获取InoutString
        InputStream inputStream = multipartFile.getInputStream();
        // 创建文件
        File toFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        // 写入文件
        OutputStream outputStream = new FileOutputStream(toFile);
        byte[] buffer = new byte[8192];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        return toFile;
    }

    //获取文件类型
    public String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;//warning添加
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

    public boolean downloadFile(byte[] data, String fileName, String fileExtension,HttpServletResponse response) throws UnsupportedEncodingException {
        fileName = fileName + "." + fileExtension;
        try {
            // 创建临时文件
            File tempFile = File.createTempFile(fileName, ".tmp");

            // 将byte[]数组写入临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
            }

            // 设置响应头，告诉浏览器返回的是一个文件
            // 根据文件扩展名设置Content-Type
            String contentType = "application/octet-stream;charset=UTF-8"; // 默认为通用的二进制文件类型
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");

            // 获取响应输出流
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                // 将文件发送给客户端
                java.nio.file.Files.copy(tempFile.toPath(), outputStream);
                outputStream.flush();
            }

            // 删除临时文件
            tempFile.delete();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
            // 处理异常
        }
    }
}
