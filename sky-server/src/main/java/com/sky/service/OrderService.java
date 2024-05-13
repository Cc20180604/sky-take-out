package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.vo.OrdersAndDetailVO;

public interface OrderService {
    /**
     * 储存订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO saveOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 分页查询订单
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    /**
     * 用户取消自己的订单
     * @param id
     */
    void cancel(Long id);

    /**
     * 查询自己的订单详情
     * @param id
     * @return
     */
    OrderVO orderDetail(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repeat(Long id);

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void cancelService(OrdersCancelDTO ordersCancelDTO);

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     */
    void reject(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 统计订单数目
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     * @param id
     */
    void confirm(Long id);

    /**
     * 派送订单
     * @param id
     */
    void delivery(Long id);

    /**
     * 催单
     * @param id
     */
    void reminder(Long id);
}
