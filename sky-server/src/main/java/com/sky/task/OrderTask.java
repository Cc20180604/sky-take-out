package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;
    /**
     * 每一分钟取消所有支付超时订单
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void cancelProgram() {
        //超出15分钟未支付订单直接取消
        ordersMapper.updateStatus(Orders.CANCELLED, Orders.builder().payStatus(Orders.UN_PAID).orderTime(LocalDateTime.now().minusMinutes(-15)).build());
    }

    /**
     * 每天一点取消所有派送中订单
     * 我觉的不合理 未实现
     */
//    @Scheduled(fixedRate = 5000)
//    @Async
//    public void task2() {
//        Thread.currentThread().setName("task2");
//        log.info(Thread.currentThread().getName());
//    }
}
