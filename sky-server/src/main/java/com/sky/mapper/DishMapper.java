package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.github.pagehelper.Page;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 分页查询
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param dishId
     * @return
     */
    DishVO selectDishVOById(Long dishId);

    /**
     * 动态更新菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 批量删除菜品
     */
    void deleteBatch(List<Long> dishIds);
    /**
     * 根据id查询菜品
     * @param dishId
     */
    @Select("select * from dish where id = #{dishId}")
    Dish selectById(Long dishId);

    /**
     * 批量查询菜品
     * @param dishIds
     * @return
     */
    List<Dish> selectByIds(List<Long> dishIds);

    /**
     * 根据菜品种类查询
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> selectByCategoryId(Long categoryId);

    /**
     * 根据菜品状态查询数目
     * @param status
     * @return
     */
    @Select("select count(*) from dish where status = #{status}")
    Integer countByStatus(int status);
}
