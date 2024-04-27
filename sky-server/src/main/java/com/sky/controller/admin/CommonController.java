package com.sky.controller.admin;

import com.aliyuncs.http.HttpResponse;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.service.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RestController
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private CommonService commonService;

    /**
     * 图片上传
     * @param file
     * @return
     */
    @PostMapping("/admin/common/upload")
    @ApiOperation("图片上传")
    public Result<String> upload(MultipartFile file){
        log.info("上传图片:{}",file.getOriginalFilename());
        String saveUrl = null;
        try {
            saveUrl = commonService.saveImg(file);
        } catch (IOException e) {
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success(saveUrl);
    }

    /**
     * 图片下载
     * @param filename
     * @param response
     * @throws IOException
     */
    @RequestMapping("/img/{filename}")
    @ApiOperation("图片下载")
    public void img(@PathVariable String filename, HttpServletResponse response) throws IOException {
        log.info("下载图片:{}",filename);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        InputStream inputStream = commonService.getImg(filename);
        IOUtils.copy(inputStream, response.getOutputStream());
    }
}
