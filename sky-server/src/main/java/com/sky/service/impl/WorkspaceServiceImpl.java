package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public BusinessDataVO businessData() {
        LocalDateTime earliest = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime last = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        //新增用户
        Integer newUsers = userMapper.countUser(LocalDateTime.of(LocalDate.now(), LocalTime.MIN) , LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
        //TODO 营业额浮点型
        Integer turnoverInt = ordersMapper.sum(earliest, last);
        if (turnoverInt == null){
            turnoverInt = 0;
        }
        Double turnover = Double.valueOf(turnoverInt.toString());
        //有效订单数
        Integer validOrderCount = ordersMapper.validOrderCount(earliest, last);
        //总订单数
        Integer orderCount = ordersMapper.orderCount(earliest, last);
        //订单完成率
        Double orderCompletionRate = (double) 0;
        //平均客单价
        Double unitPrice = (double) 0;
        //平均客单价 订单完成率
        if ( orderCount != null && orderCount != 0){
            orderCompletionRate = validOrderCount / Double.valueOf(orderCount.doubleValue());
            unitPrice = turnover / orderCount;
        }


        BusinessDataVO businessDataVO = BusinessDataVO.builder()
                .newUsers(newUsers)
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .build();
        return businessDataVO;
    }

    /**
     * 统计套餐数据
     * @return
     */
    @Override
    public SetmealOverViewVO setmeals() {
        Integer enableSetmeal = setmealMapper.setmealCountByStatus(0);
        Integer disableSetmeal = setmealMapper.setmealCountByStatus(1);
        return SetmealOverViewVO.builder().sold(enableSetmeal).discontinued(disableSetmeal).build();
    }

    /**
     * 统计菜品启用禁用数目
     * @return
     */
    @Override
    public DishOverViewVO dish() {
        Integer dishDisableNum = dishMapper.countByStatus(StatusConstant.DISABLE);
        Integer dishEnableNum = dishMapper.countByStatus(StatusConstant.ENABLE);
        return DishOverViewVO.builder().sold(dishEnableNum).discontinued(dishDisableNum).build();
    }

    /**
     * 统计订单数
     * @return
     */
    @Override
    public OrderOverViewVO order() {
        LocalDateTime earliest = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime latest = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Integer all = ordersMapper.countByOrders(Orders.builder().build(), earliest, latest);
        Integer cancel = ordersMapper.countByOrders(Orders.builder().status(Orders.CANCELLED).build(), earliest, latest);
        Integer complete = ordersMapper.countByOrders(Orders.builder().status(Orders.COMPLETED).build(), earliest, latest);
        Integer deliver = ordersMapper.countByOrders(Orders.builder().status(Orders.DELIVERY_IN_PROGRESS).build(), earliest, latest);
        Integer waiting = ordersMapper.countByOrders(Orders.builder().status(Orders.TO_BE_CONFIRMED).build(), earliest, latest);

        return OrderOverViewVO.builder()
                .allOrders(all)
                .cancelledOrders(cancel)
                .completedOrders(complete)
                .deliveredOrders(deliver)
                .waitingOrders(waiting)
                .build();
    }
}
