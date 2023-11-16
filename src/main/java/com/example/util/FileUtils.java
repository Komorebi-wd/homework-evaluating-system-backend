package com.example.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtils {
    /**
     * 文件名长度
     */
    private static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /**
     * 允许的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {
            "jpg", "img", "png", "gif"
    };

    /**
     * 项目路径
     */
    private static final String UPLOADED_FOLDER = "E:/Project/JavaProject/sb_demo1/src/main/resources/cache";


    /**
     * 上传方法
     */
    public String upload(MultipartFile file) throws Exception {
        int len = file.getOriginalFilename().length();//文件名长度
        if (len > DEFAULT_FILE_NAME_LENGTH) {
            throw new Exception("文件名超出限制!");
        }
        String extension = getExtension(file);//文件格式
        if(!isValidExtension(extension)){
            throw new Exception("文件格式不正确");
        }
        // 自定义文件名
        String filename = getPathName(file);//随机名，带格式
        // 获取file对象
        File desc = getFile(filename);
        // 写入file
        file.transferTo(desc);
        return filename;
    }

    /**
     * 获取file对象
     */
    //在预设的路径下创建文件
    private File getFile(String filename) throws IOException {
        File file = new File(UPLOADED_FOLDER + "/" + filename);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        if(!file.exists()){
            file.createNewFile();
        }
        return file;
    }

    /**
     * 验证文件类型是否正确
     */
    private boolean isValidExtension(String extension) {
        for (String allowedExtension : ALLOWED_EXTENSIONS) {
            if(extension.equalsIgnoreCase(allowedExtension)){
                return true;
            }
        }
        return false;
    }

    /**
     * 此处自定义文件名,uuid + extension
     */
    private String getPathName(MultipartFile file) {
        String extension = getExtension(file);
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * 获取扩展名
     */
    private String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }
}
