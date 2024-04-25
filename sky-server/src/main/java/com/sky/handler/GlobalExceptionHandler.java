package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @Value("${file.path.img}")
    String imgPath;
    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理sql异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){

        String message = ex.getMessage();
        //Duplicate entry '石敏' for key 'idx_username'
        if (message.contains("Duplicate entry")){
            String duplicateValue = message.split(" ")[2];
            String entry = message.split(" ")[5];
            log.error("约束{}已有重复的值：{}", entry, duplicateValue);
            return Result.error(duplicateValue+MessageConstant.ALREADY_EXISTS);
        }

        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

    /**
     * 处理文件获取异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result fileExceptionHandler(FileNotFoundException ex){

        String message = ex.getMessage();
        //D:\Download\file\img\44dc9e65-76b7-4ece-afbd-8c2b7fbd29f7.jpg (系统找不到指定的文件。)
        if (message.contains(imgPath)){
            log.error(message);
            return Result.error(MessageConstant.IMG_NOT_FOUND);
        }

        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
