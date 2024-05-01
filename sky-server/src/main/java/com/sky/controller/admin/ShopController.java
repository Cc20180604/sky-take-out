package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    //店铺状态字段
    private static final String statusKey = "SHOP_STATUS";
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result status(@PathVariable Integer status){
        log.info("设置店铺状态为:{}",status == StatusConstant.ENABLE ? "营业中" : "歇业");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(statusKey, status);
        return Result.success();
    }
    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status = (Integer) valueOperations.get(statusKey);
        log.info("店铺状态为:{}",status == StatusConstant.ENABLE ? "营业中" : "歇业");
        return Result.success(status);
    }
}
