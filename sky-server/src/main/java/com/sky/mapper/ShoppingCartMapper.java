package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    ShoppingCart selectByUIDAndShoppingCard(Long uid, ShoppingCartDTO shoppingCartDTO);
    @AutoFill(OperationType.UPDATE)
    void update(ShoppingCart shoppingCart);

    @AutoFill(OperationType.INSERT)
    void insert(ShoppingCart shoppingCart);

    @Select("select * from shopping_cart where user_id = #{uid}")
    List<ShoppingCart> list(Long uid);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from shopping_cart where user_id = #{uid}")
    void deleteByUserId(Long uid);
}
