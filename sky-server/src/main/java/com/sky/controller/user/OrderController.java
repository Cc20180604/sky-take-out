package com.sky.controller.user;


import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.vo.OrdersAndDetailVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("UserOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("提交订单:{}", ordersSubmitDTO);
        //将订单信息存储到到订单表 和 订单详细表
        OrderSubmitVO orderSubmitVO = orderService.saveOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("/payment")
    @ApiOperation("支付")
    public Result payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO){
        log.info("支付:{}", ordersPaymentDTO);
        //TODO 未接入wx支付接口无法回调 支付直接成功
        List<Orders> orders = ordersMapper.pageQuery(Orders.builder().number(ordersPaymentDTO.getOrderNumber()).build()).getResult();
        if (orders.isEmpty()){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        ordersMapper.update(Orders.builder().id(orders.get(0).getId()).status(Orders.TO_BE_CONFIRMED).payStatus(Orders.PAID).build());


        //socket通知前端
        Map map = new HashMap<>();
        //1 来单提醒 2 客户接单
        map.put("type",1);
        map.put("orderId",orders.get(0).getId());
        map.put("content","订单号:"+orders.get(0).getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
        OrderPaymentVO orderPaymentVO = OrderPaymentVO.builder()
                .nonceStr(System.currentTimeMillis()+"")
                .packageStr("123")
                .paySign("123")
                .timeStamp(System.currentTimeMillis()+"")
                .signType("666")
                .build();
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("分页查询")
    public Result<PageResult> pageList(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("分页查询订单:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id){
        log.info("取消订单:{}",id);
        orderService.cancel(id);
        return Result.success();
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> orderDetail(@PathVariable("id") Long id){
        log.info("查看订单详情:id");
        OrderVO orderVO = orderService.orderDetail(id);
        return Result.success(orderVO);
    }


    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable("id") Long id){
        log.info("再来一单:{}",id);
        orderService.repeat(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable("id") Long id){
        log.info("催单:{}",id);
        orderService.reminder(id);
        return Result.success();
    }
}
