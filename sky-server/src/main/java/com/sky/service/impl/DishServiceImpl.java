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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.sky.constant.MessageConstant.DISH_IN_ENABLE_SETMEA;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    RedisTemplate redisTemplate;
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
    @CacheEvict(cacheNames = "dish", key = "#dishDTO.categoryId")
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

        //删除分类缓存
        //String redisKey = "dish_"+dishDTO.getCategoryId();
        //redisTemplate.delete(redisKey);
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
     * 根据分类id获取所有菜品vo
     * @param categoryId
     * @return
     */
    @Override
    @Cacheable(cacheNames = "dish", key = "#categoryId")
    public List<DishVO> selectDishVOListByCategoryId(Long categoryId) {
        String dishVOListKey = "dish_"+categoryId;
        //ValueOperations valueOperations = redisTemplate.opsForValue();
        //List<DishVO> dishVOList = (List<DishVO>) valueOperations.get(dishVOListKey);
        /*************在缓存中, 直接返回缓存中的数据*************/
        //if (dishVOList != null && !dishVOList.isEmpty()){
        //    return dishVOList;
        //}


        /*************不在缓存中, 从数据库获取然后加入缓存中*************/
        List<Dish> dishList = dishMapper.selectByCategoryId(categoryId);
        //所有dishId
        List<Long> dishIdList = new ArrayList<>();
        for (Dish dish : dishList){
            dishIdList.add(dish.getId());
        }

        //菜品对应口味list
        List<DishFlavor> dishFlavorList = dishFlavorMapper.selectByDishIds(dishIdList);
        Map<Long, List<DishFlavor>> flavorMap = new HashMap<>();
        for (DishFlavor dishFlavor : dishFlavorList){
            Long dishId = dishFlavor.getDishId();
            //map中没有该菜品
            if (!flavorMap.containsKey(dishId)){
                List<DishFlavor> thisDishFlavors = new ArrayList<>();
                thisDishFlavors.add(dishFlavor);
                flavorMap.put(dishId, thisDishFlavors);
            }else {
                flavorMap.get(dishId).add(dishFlavor);
            }
        }
        //将菜品与对应口味 转为dishVO
        List<DishVO> dishVOList = new ArrayList<>();
        for(Dish dish : dishList){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish,dishVO);
            //加入菜品口味
            dishVO.setFlavors(flavorMap.get(dish.getId()));
            dishVOList.add(dishVO);
        }
        //加入缓存
        //valueOperations.set(dishVOListKey, dishVOList);
        return dishVOList;
    }

    /**
     * 更新菜品信息
     * @param dishDTO
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "dish", allEntries = true)
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

        //删除全部分类缓存
        cleanCache("dish_*");
    }
    /**
     * 批量删除菜品和对应口味
     * @param dishIds
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "dish", allEntries = true)
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



        //删除全部分类缓存
        cleanCache("dish_*");
    }
    /**
     * 改变菜品状态
     * @param status
     */
    @Override
    @CacheEvict(cacheNames = "dish", allEntries = true)
    public void status(Long id, Integer status) {
        //停售操作
        List<SetmealDish> setmealDishes = setmealDishMapper.selectByDishId(id);
        //停售需要该菜品套餐 已停售/不在套餐中
        if (status == StatusConstant.DISABLE && !setmealDishes.isEmpty()){

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
                for (Setmeal setmeal : setmeals){
                    //菜品在启售的套餐中无法被停售
                    if (setmeal.getStatus() == StatusConstant.ENABLE){
                        String tips = DISH_IN_ENABLE_SETMEA(setmeal.getName());
                        log.error(tips);
                        throw new DeletionNotAllowedException(tips);
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

        //删除全部分类缓存
        //cleanCache("dish_*");


    }

    /**
     * 清除redis缓存
     * @param pattern
     */
    private void cleanCache(String pattern){
        redisTemplate.delete(redisTemplate.keys(pattern));
    }
}
