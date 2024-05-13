package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 通过套餐id查询
     * @param setMealId
     * @return
     */
    @Select("select * from setmeal where id = #{setMealId}")
    Setmeal selectById(Long setMealId);

    /**
     * 批量查询套餐
     * @param setmealIds
     */
    List<Setmeal> selectByIds(List<Long> setmealIds);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 分页查询
     * @param setmeal
     */
    Page<Setmeal> pageQuery(SetmealPageQueryDTO setmeal);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新套餐
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 查询分类id下的所有套餐
     * @param categoryId
     * @return
     */
    @Select("select * from setmeal where category_id = #{categoryId} and status = 1")
    List<Setmeal> selectByCategoryId(String categoryId);

    /**
     * 统计对应状态的订单数目
     * @param status
     * @return
     */
    @Select("select count(*) from setmeal where status = #{status}")
    int setmealCountByStatus(Integer status);
}
