package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面 公共字段自动填充
 */
@Slf4j
@Component
@Aspect
public class AutoFillAspect {
    /**
     * 切面
     *  * com.sky.mapper    .*      .*      (..)
     *            mapper包下 所有类 所有注解 所有返回值
     *  && com.sky.annotation.AutoFill
     *  并且有AutoFill注解
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill) ")
    public void autoFillPointCut(){}

    /**
     * 前置通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        Object[] obj = joinPoint.getArgs();
        //获取数据操作类型

        //方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //数据库操作类型
        OperationType operationType = autoFill.value();

        //获取参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length ==0){
            return;
        }
        //第一个参数为实体类
        Object entity = args[0];


        Long id = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        if (operationType == OperationType.INSERT){
            log.info("INSERT公共字段填充");
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //由于不确定类名, 通过反射机制给实体类赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, id);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }else if(operationType == OperationType.UPDATE){
            log.info("UPDATE公共字段填充");
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //由于不确定类名, 通过反射机制给实体类赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, id);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }


    }
}
