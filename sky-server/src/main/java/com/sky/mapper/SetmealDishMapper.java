package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品查询套餐
     * @param dishId
     * @return
     */
    @Select("select * from setmeal_dish where dish_id = #{dishId}")
    List<SetmealDish> selectByDishId(Long dishId);

    /**
     * 根据菜品id集合 查询所有关联的套餐
     * @param dishIds
     * @return
     */
    List<SetmealDish> selectByDishIds(List<Long> dishIds);

    /**
     * 批量插入菜品套餐关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id 查询所有关系
     * @param id
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> selectBySetmealId(Long id);

    /**
     * 删除所有套餐对应的关系
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
