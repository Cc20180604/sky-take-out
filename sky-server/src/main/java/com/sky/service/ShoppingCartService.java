package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 在购物车中添加商品
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 获取用户购物车中所有商品
     * @return
     */
    List<ShoppingCart> list(Long uid);

    /**
     * 删除购物车中的一个菜品
     * @param shoppingCartDTO
     */
    void subService(Long uid, ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     * @param uid
     */
    void clean(Long uid);
}
