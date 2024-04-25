package com.sky.service.impl;

import com.sky.service.CommonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class CommonServiceImpl implements CommonService {
    @Value("${file.path.img}")
    String imgPath;
    @Value("${file.location}")
    String location;
    @Override
    public String saveImg(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String name = file.getOriginalFilename();
        //文件后缀 .png
        String suffix = name.substring(name.lastIndexOf('.'));
        //存储文件
        file.transferTo(new File(imgPath+uuid+suffix));
        return location+"img/"+uuid+suffix;
    }

    /**
     * 获取图片输入流
     * @param imgName
     * @return
     */
    @Override
    public InputStream getImg(String imgName) {
        InputStream inputStream = null;
        File file = new File(imgPath+imgName);
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }
}
