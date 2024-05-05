package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "用户购物车相关接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @PostMapping("/add")
    @ApiOperation("加入购物车")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("获取购物车集合")
    public Result<List<ShoppingCart>> list(){
        Long uid = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(uid);
        return Result.success(shoppingCarts);
    }

    @PostMapping("/sub")
    @ApiOperation("删除一个菜品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        Long uid = BaseContext.getCurrentId();
        //删除购物车中的一个菜品
        shoppingCartService.subService(uid,shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空用户购物车")
    public Result clean(){
        Long uid = BaseContext.getCurrentId();
        //删除购物车中的一个菜品
        shoppingCartService.clean(uid);
        return Result.success();
    }

}
