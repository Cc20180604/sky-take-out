package com.sky.service;

import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface CommonService {
    public String saveImg(MultipartFile file) throws IOException;

    /**
     * 获取图片输入流
     * @param imgName
     * @return
     */
    public InputStream getImg(String imgName);
}
