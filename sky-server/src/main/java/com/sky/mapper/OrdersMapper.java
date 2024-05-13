package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 分页查询订单
     * @param orders
     * @return
     */
    Page<Orders> pageQuery(Orders orders);

    /**
     * 更新订单数据
     * @param orders
     */
    void update(Orders orders);

    /**
     * 指定状态订单数目
     * @param i
     * @return
     */
    @Select("select count(*) from orders where status = #{i}")
    Integer count(int i);

    /**
     * 批量更新指定订单状态
     */
    Integer updateStatus(Integer status,Orders orders);

    /**
     * 根据订单状态 与 超过订单时间
     * 查询订单
     */
    @Select("SELECT * FROM orders WHERE status = #{status} AND order_time < #{orderTime} ")
    List<Orders> selectByStatusAndOrderTime(Orders order);

    /**
     * 求出时间段的营业额
     * @return
     */
    @Select("select sum(amount) from orders where order_time > #{begin} and order_time < #{end} and status = 5")
    Integer sum(LocalDateTime begin, LocalDateTime end);

    /**
     * 求出每日订单数
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from orders where order_time between #{begin} and #{end}")
    Integer orderCount(LocalDateTime begin, LocalDateTime end);
    /**
     * 求出之前的订单总数
     * @param before
     * @return
     */
    @Select("select count(*) from orders where order_time <= #{before}")
    Integer orderBeforeCount(LocalDateTime before);
    /**
     * 求出有效订单数
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from orders where order_time >= #{begin} and order_time <=#{end} and status = 5")
    Integer validOrderCount(LocalDateTime begin, LocalDateTime end);

    /**
     * 求之前的有效订单总数
     * @param before
     * @return
     */
    @Select("select count(*) from orders where order_time <= #{before}")
    Integer validOrderCountBefore(LocalDateTime before);


    /**
     * 统计订单数目
     * @param orders
     * @return
     */
    Integer countByOrders(Orders orders, LocalDateTime begin, LocalDateTime end);
}
