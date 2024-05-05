package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishFlavorMapper {
    /**
     * 新增口味
     * @param dishFlavor
     */
    @Insert("insert into dish_flavor (dish_id, name, value) values (#{dishId}, #{name}, #{value})")
    public void insert(DishFlavor dishFlavor);

    /**
     * 批量插入口味
     * @param dishFlavors
     */
    void insertBatch(List<DishFlavor> dishFlavors);

    /**
     * 批量删除dishId
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);
    /**
     * 查询菜品的所有口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectByDishId(Long dishId);

    /**
     * 批量删除菜品
     * @param dishIds
     */
    void deleteBatch(List<Long> dishIds);

    /**
     * 批量获取菜品口味
     * @param dishIdList
     * @return
     */
    @MapKey("dishId")
    List<DishFlavor> selectByDishIds(List<Long> dishIdList);
}
