package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //将套餐存入套餐表
        setmealMapper.insert(setmeal);

        //设置setmealId
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmeal.getId());
        }

        //更新菜品关系表
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开启分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        //查询条件
        Setmeal setmealQuery = new Setmeal();

        Page<Setmeal> setmealsPage = setmealMapper.pageQuery(setmealPageQueryDTO);
        //套餐实体类集合
        List<Setmeal> setmealList = setmealsPage.getResult();

        //无数据直接返回
        if (setmealList.size() == 0){
            return new PageResult(0, new ArrayList());
        }


        //转为SetmealVO集合
        //遍历获取分类id集合
        List<Long> categoryIds = new ArrayList<>();
        for (Setmeal setmeal: setmealList){
            categoryIds.add(setmeal.getCategoryId());
        }
        //有重复种类 in会自动过滤
        List<Category> categoryList = categoryMapper.selectByIds(categoryIds);
        //转为map
        HashMap<Long, Category> categoryHashMap = new HashMap<>();
        for (Category category : categoryList){
            categoryHashMap.put(category.getId(),category);
        }
        //遍历套餐实体类集合
        //setmeal转为setmealVO集合
        List<SetmealVO> setmealVOList = new ArrayList<>();
        for (int i =0; i< setmealList.size(); i++){
            SetmealVO setmealVO = new SetmealVO();
            //转化为vo
            BeanUtils.copyProperties(setmealList.get(i), setmealVO);
            setmealVOList.add(setmealVO);
            //setCategoryName
            setmealVOList.get(i).setCategoryName(categoryHashMap.get(setmealVO.getCategoryId()).getName());
        }
        return new PageResult(setmealsPage.getTotal(), setmealVOList);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //删除套餐表
        setmealMapper.deleteBatch(ids);
        //删除套餐菜品关系表
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 通过id获取套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        //根据id获取setmeal实体类
        Setmeal setmeal = setmealMapper.selectById(id);
        //根据id获取对应的套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDishMapper.selectBySetmealId(id);

        //实体类转为setmealVO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 更新套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //将套餐存入套餐表
        setmealMapper.update(setmeal);


        //更新菜品关系表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //1.删除该套餐的所有关系
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        //2.插入新的套餐关系
        for (SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 设置套餐状态
     * @param status
     */
    @Override
    public void setStatus(Long id,Integer status) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();

        setmealMapper.update(setmeal);

    }

}
