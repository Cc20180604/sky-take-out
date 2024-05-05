package com.sky.controller.user;

import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("UserSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @GetMapping("/list")
    @ApiOperation("根据分类id获取套餐")
    public Result<List<Setmeal>> getByCategoryId(String categoryId){
        List<Setmeal> setmealList = setmealService.getByCategoryId(categoryId);
        return Result.success(setmealList);
    }


    @GetMapping("/dish/{id}")
    @ApiOperation("获取套餐内所有菜品")
    public Result<List<DishItemVO>> getBySetmealId(@PathVariable Long id){
        List<DishItemVO> dishItemVOS = setmealService.getDishesBySetmealId(id);
        return Result.success(dishItemVOS);
    }
}
