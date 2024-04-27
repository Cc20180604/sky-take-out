package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //存储菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        //菜品id
        Long dishId = dish.getId();
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if (dishFlavors != null && !dishFlavors.isEmpty()){
            //更新每个口味的菜品id
            Iterator<DishFlavor> iterator = dishFlavors.iterator();
            while (iterator.hasNext()){
                iterator.next().setDishId(dishId);
            }
            //批量存储该菜品口味
            dishFlavorMapper.insertBatch(dishFlavors);
        }

    }
    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishPage = dishMapper.pageQuery(dishPageQueryDTO);


        //更新dishVOS中的口味集合
        //Iterator<DishVO> iterator = dishPage.getResult().iterator();
        //while (iterator.hasNext()){
        //    DishVO dishVO = iterator.next();
        //    //通过菜品id获取每个菜品口味集合
        //    List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(Long.valueOf(dishVO.getId()));
        //    dishVO.setFlavors(flavors);
        //}
        return new PageResult(dishPage.getTotal(), dishPage.getResult());
    }

    /**
     * 根据菜品id查询菜品
     * @param dishId
     * @return
     */
    @Override
    public DishVO selectDishVOById(Long dishId) {
        //查询该菜品口味
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(dishId);
        //查询该菜品
        DishVO dishVO = dishMapper.selectDishVOById(dishId);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 更新菜品信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        //更新菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //更新口味表
        if (flavors != null && !flavors.isEmpty()){
            //删除所有原来的口味
            dishFlavorMapper.deleteByDishId(dishDTO.getId());
            //批量加入新口味
            //更新每个口味的菜品id
            Iterator<DishFlavor> iterator = flavors.iterator();
            while (iterator.hasNext()){
                iterator.next().setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
