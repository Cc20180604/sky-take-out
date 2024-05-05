package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据菜品id查询菜品
     * @param dishId
     * @return
     */
    DishVO selectDishVOById(Long dishId);

    /**
     * 根据菜品种类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> selectDishByCategoryId(Long categoryId);
    /**
     * 根据菜品种类id查询菜品vo集合
     * @param categoryId
     * @return
     */
    List<DishVO> selectDishVOListByCategoryId(Long categoryId);
    /**
     * 更新菜品与口味表
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     * 批量删除菜品和对应口味
     * @param dishIds
     */
    void deleteDishesAndFlavors(List<Long> dishIds);

    /**
     * 改变菜品状态
     * @param status
     */
    void status(Long id ,Integer status);
}
