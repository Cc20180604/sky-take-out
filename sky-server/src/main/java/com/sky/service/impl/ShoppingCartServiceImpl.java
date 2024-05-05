package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.DishService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.JwtUtil;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    /**
     * 在购物车中添加商品
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long uid = BaseContext.getCurrentId();
        //查询该菜品或套餐
        ShoppingCart shoppingCart = shoppingCartMapper.selectByUIDAndShoppingCard(uid,shoppingCartDTO);
        //不为null说明第二次加入 只需要数量+1
        if (shoppingCart != null){
            shoppingCart.setNumber(shoppingCart.getNumber()+1);
            shoppingCartMapper.update(shoppingCart);
            return;
        }

        //为null说明是新菜品或套餐
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        //加入新菜品
        if (dishId != null){
            Dish dish = dishMapper.selectById(dishId);
            //String amount = shoppingCartDTO. + dish.getPrice();
            shoppingCart = ShoppingCart.builder()
                    .name(dish.getName())
                    .userId(uid)
                    .dishId(dishId)
                    .dishFlavor(shoppingCartDTO.getDishFlavor())
                    .number(1)
                    .amount(dish.getPrice())
                    .image(dish.getImage())
                    .build();
        //加入新套餐
        } else if (setmealId != null) {
            Setmeal setmeal = setmealMapper.selectById(setmealId);
            //String amount = shoppingCartDTO. + dish.getPrice();
            shoppingCart = ShoppingCart.builder()
                    .name(setmeal.getName())
                    .userId(uid)
                    .setmealId(setmealId)
                    .dishFlavor(shoppingCartDTO.getDishFlavor())
                    .number(1)
                    .amount(setmeal.getPrice())
                    .image(setmeal.getImage())
                    .build();
        }
        //加入购物车表
        //新增购物车数据
        shoppingCartMapper.insert(shoppingCart);
    }
    /**
     * 获取用户购物车中所有商品
     * @return
     */
    @Override
    public List<ShoppingCart> list(Long uid) {
        return shoppingCartMapper.list(uid);
    }

    /**
     * 删除购物车中的一个菜品
     * @param shoppingCartDTO
     */
    @Override
    public void subService(Long uid, ShoppingCartDTO shoppingCartDTO) {
        //获取该套餐或菜品
        ShoppingCart shoppingCart = shoppingCartMapper.selectByUIDAndShoppingCard(uid, shoppingCartDTO);
        //减一后的数据
        Integer afterNum = shoppingCart.getNumber() - 1;
        //-1 为0直接删除 清除该菜品
        if (afterNum == 0){
            shoppingCartMapper.deleteById(shoppingCart.getId());

        //不为大于0 更新个数
        }else {
            shoppingCart.setNumber(afterNum);
            shoppingCartMapper.update(shoppingCart);
        }

    }
    /**
     * 清空购物车
     * @param uid
     */
    @Override
    public void clean(Long uid) {
        shoppingCartMapper.deleteByUserId(uid);
    }
}
