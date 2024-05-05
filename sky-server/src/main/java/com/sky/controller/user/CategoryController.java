package com.sky.controller.user;


import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("CategoryController")
@Api(tags = "种类")
@Slf4j
@RequestMapping("/user/category")
public class CategoryController {
    private static final String DISH_TYPE = "1";
    private static final String SETMEAL_TYPE = "2";
    @Autowired
    private CategoryService categoryService;
    /**
     * 获取套餐分类 与 菜品分类
     * @param type null为获取所有
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> list(String type){
        List<Category> categoryList = null;
        if (DISH_TYPE.equals(type) || SETMEAL_TYPE.equals(type)){
            //获取指定分类
            categoryList = categoryService.list(Integer.valueOf(type));
        }else if (type == null){
            //null获取所有出售中的分类
            categoryList = categoryService.list();
        }else {
            throw new BaseException(MessageConstant.TYPE_NOT_FOUND);
        }
        return Result.success(categoryList);
    }
}
