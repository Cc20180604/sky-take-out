package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
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
     * 根据菜品种类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> selectDishByCategoryId(Long categoryId) {
        //查询该菜品
        List<Dish> dish = dishMapper.selectByCategoryId(categoryId);
        return dish;
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
    /**
     * 批量删除菜品和对应口味
     * @param dishIds
     */
    @Override
    @Transactional
    public void deleteDishesAndFlavors (List<Long> dishIds) {
        List<Dish> dishes = dishMapper.selectByIds(dishIds);
        for (Dish dish : dishes){
            //启售中的菜品无法被删除
            if (dish.getStatus() == StatusConstant.ENABLE){
                String errorMessage = MessageConstant.DISH_ON_SALE+"("+dish.getName()+")";
                log.error(errorMessage);
                throw new DeletionNotAllowedException(errorMessage);
            }

        }

        //被套餐关联的菜品也无法被删除
        List<SetmealDish> setmealDishes = setmealDishMapper.selectByDishIds(dishIds);

        if (setmealDishes != null && !setmealDishes.isEmpty()){
            //TODO 测试菜品在套餐中无法被删除
            //错误提示第一个套餐
            SetmealDish setmealDish = setmealDishes.get(0);
            String dishName = dishMapper.selectById(setmealDish.getDishId()).getName();
            String setmealName = setmealMapper.selectById(setmealDish.getSetmealId()).getName();

            String errorMessage = MessageConstant.DISH_BE_RELATED_BY_SETMEAL+": "+dishName+"在套餐"+ setmealName +"中";
            log.error(errorMessage);
            throw new DeletionNotAllowedException(errorMessage);
        }


        //删除所有菜品
        dishMapper.deleteBatch(dishIds);
        //删除所有对应的口味
        dishFlavorMapper.deleteBatch(dishIds);
    }
    /**
     * 改变菜品状态
     * @param status
     */
    @Override
    public void status(Long id, Integer status) {
        //停售操作
        //停售需要该菜品不在套餐中/套餐已停售
        if (status == StatusConstant.DISABLE){
            List<SetmealDish> setmealDishes = setmealDishMapper.selectByDishId(id);

            //菜品所有对应的套餐id
            List<Long> setmealIds = new ArrayList<>();
            for (SetmealDish setmealDish : setmealDishes){
                setmealIds.add(setmealDish.getSetmealId());
            }

            //获取所有该菜品所有对应的套餐
            List<Setmeal> setmeals = setmealMapper.selectByIds(setmealIds);

            //该菜品在套餐中
            if (setmeals != null && !setmeals.isEmpty()){
                //查询是否有启售的套餐
                //TODO 未测试套餐中的菜品能被删除
                for (Setmeal setmeal : setmeals){
                    if (setmeal.getStatus() != StatusConstant.ENABLE){
                        String dishName = dishMapper.selectById(id).getName();
                        String setmealName = setmeal.getName();
                        String message = MessageConstant.DISH_ON_SALE+": 套餐【"+setmealName+"】启售中, 菜品【"+dishName+"】无法被删除";
                        log.error(message);
                        throw new DeletionNotAllowedException(message);
                    }
                }


                ////判断所有套餐是否停售
                //for (SetmealDish setmealDish:setmealDishes){
                //    //套餐id
                //    Long setmealId = setmealDish.getSetmealId();
                //    Setmeal setmeal = setmealMapper.selectById(setmealId);
                //    //有套餐启售中,则菜品不允许停售
                //    if (setmeal.getStatus() == StatusConstant.ENABLE){
                //        String dishName = dishMapper.selectById(id).getName();
                //        String setmealName = setmeal.getName();
                //        String message = MessageConstant.DISH_ON_SALE+": 套餐【"+setmealName+"】启售中, 菜品【"+dishName+"】无法被删除";
                //        log.error(message);
                //        throw new DeletionNotAllowedException(message);
                //    }
                //}

            }
        }

        //改变售出状态
        Dish dish = Dish.builder().id(id).status(status).build();
        dishMapper.update(dish);

    }
}
