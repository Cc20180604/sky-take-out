package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.vo.OrdersAndDetailVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    /**
     * 判断订单是否存在
     * @param orders
     * @return
     */
    private boolean orderExists(Orders orders) {
        List<Orders> order = ordersMapper.pageQuery(orders).getResult();
        return order != null && !order.isEmpty();
    }
    /**
     * 将订单数据保存到订单表 详细菜品保存到订单详细表
     * @param ordersSubmitDTO 订单数据
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO saveOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long uid = BaseContext.getCurrentId();
        //排除地址簿数据为空 购物车为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(uid);
        if (shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //1.保存到订单表
        String orderNumber = String.valueOf(System.currentTimeMillis());
        LocalDateTime orderTime = LocalDateTime.now();
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //下单时间
        orders.setOrderTime(orderTime);
        //支付状态
        orders.setPayStatus(Orders.UN_PAID);
        //订单状态
        orders.setStatus(Orders.PENDING_PAYMENT);
        //地址簿电话号码
        orders.setPhone(addressBook.getPhone());
        //收货人
        orders.setConsignee(addressBook.getConsignee());
        //订单号
        orders.setNumber(orderNumber);
        //用户id
        orders.setUserId(uid);
        //下单同户名
        User user = userMapper.selectById(uid);
        orders.setUserName(user.getName());
        //地址簿地址
        orders.setAddress(addressBook.getDetail());
        //id赋值给orders
        ordersMapper.insert(orders);

        //2.保存到订单详细表
        //获取购物车中所有菜品与套餐
        List<OrderDetail> orderDetails = new ArrayList<>();
        //订单总价
        //TODO 打包费用 配送费没动态化
        BigDecimal orderAmount = new BigDecimal(ordersSubmitDTO.getPackAmount() + 6);
        for (ShoppingCart shoppingCart : shoppingCartList) {
            //构造订单详情对象
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetail.setId(null);
            orderDetails.add(orderDetail);
            //计算购物车中菜品与套餐总价
            orderAmount = orderAmount.add(orderDetail.getAmount());
        }
        //所有菜品和套餐信息转存入订单详细表
        orderDetailMapper.insertBatch(orderDetails);
        //清空购物车
        shoppingCartMapper.deleteByUserId(uid);
        return OrderSubmitVO.builder()
                .orderNumber(orderNumber)
                .orderTime(orderTime)
                .orderAmount(orderAmount)
                .build();
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //分页查询 动态查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersPageQueryDTO, orders);
        Page<Orders> ordersPage = ordersMapper.pageQuery(orders);
        PageResult pageResult = new PageResult();

        //查出订单详情
        List<Long> orderIds = new ArrayList<>();
        for (Orders order1 : ordersPage.getResult()){
            orderIds.add(order1.getId());
        }


        Map<Long, List<OrderDetail>> orderDetailMap = null;
        if(!orderIds.isEmpty()){
            List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderIds(orderIds);
            orderDetailMap = new HashMap<>();
            for (OrderDetail orderDetail : orderDetails){
                Long orderId = orderDetail.getOrderId();
                if (orderDetailMap.containsKey(orderId)){
                    orderDetailMap.get(orderId).add(orderDetail);
                }else {
                    List<OrderDetail> orderDetailList = new ArrayList<>();
                    orderDetailList.add(orderDetail);
                    orderDetailMap.put(orderId, orderDetailList);
                }
            }
        }

        //orders转为orderAndDetail
        List<OrdersAndDetailVO> ordersAndDetailVOS = new ArrayList<>();
        for (Orders order : ordersPage.getResult()){
            OrdersAndDetailVO ordersAndDetailVO = new OrdersAndDetailVO();
            BeanUtils.copyProperties(order, ordersAndDetailVO);
            //加入订单详情list
            ordersAndDetailVO.setOrderDetailList(orderDetailMap.get(order.getId()));
            ordersAndDetailVOS.add(ordersAndDetailVO);

        }

        pageResult.setTotal(ordersPage.getTotal());
        pageResult.setRecords(ordersAndDetailVOS);
        return pageResult;
    }
    /**
     * 用户取消自己的订单
     * @param id
     */
    @Override
    public void cancel(Long id) {
        //判断该订单是不是该用户的
        Page order = ordersMapper.pageQuery(Orders.builder()
                    .id(id)
                    .userId(BaseContext.getCurrentId())
                    .build());
        if (order.getResult().isEmpty()){
            throw new OrderBusinessException(MessageConstant.ORDER_CHANGE_NOT_ALLOWED);
        }
        //取消订单
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CANCELLED);
        ordersMapper.update(orders);
    }

    /**
     * 催单
     * @param id
     * @return
     */
    @Override
    public OrderVO orderDetail(Long id) {
        //判断该订单是不是该用户的
        Page orderPage = ordersMapper.pageQuery(Orders.builder()
                .id(id)
                .userId(BaseContext.getCurrentId())
                .build());
        if (orderPage.getResult().isEmpty()){
            //无权操作该订单
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //是该用户的查询订单详情表
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orderPage.getResult().get(0), orderVO);
        orderVO.setOrderDetailList(orderDetails);
        orderVO.setOrderDishes(toOrderDishesStr(orderDetails));
        return orderVO;
    }

    /**
     * 再来一单
     * @param id
     * 加入购物车中
     */
    @Override
    @Transactional
    public void repeat(Long id) {
        //判断该订单是不是该用户的
        Page orderPage = ordersMapper.pageQuery(Orders.builder()
                .id(id)
                .userId(BaseContext.getCurrentId())
                .build());
        if (orderPage.getResult().isEmpty()){
            throw new OrderBusinessException(MessageConstant.ORDER_CHANGE_NOT_ALLOWED);
        }

        //将该订单加入购物车
        Orders orders = (Orders) orderPage.getResult().get(0);
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orders.getId());
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails){
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setId(null);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }

        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //获取订单集合
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersPageQueryDTO, orders);
        Page<Orders> ordersPage = ordersMapper.pageQuery(orders);
        //过滤空
        if (ordersPage.getResult().isEmpty()){
//            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
            return new PageResult(0, new ArrayList<>());
        }
        //获取订单详情 orderList 转为 orderVOList
        List<OrderVO> orderVOList = toOrderVOList(ordersPage.getResult());
        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public void cancelService(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        ordersMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        ordersMapper.update(orders);
    }

    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     */
    @Override
    @Transactional
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        //只有订单处于“待接单”状态时可以执行拒单操作
        Orders order = ordersMapper.pageQuery(Orders.builder().id(ordersRejectionDTO.getId()).build()).getResult().get(0);
        if (order.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //商家拒单其实就是将订单状态修改为“已取消”
        Orders cancelled = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                //商家拒单时需要指定拒单原因
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .build();
        ordersMapper.update(cancelled);
        //商家拒单时，如果用户已经完成了支付，需要为用户退款
        if (order.getPayStatus() == Orders.PAID){
            //TODO 退款操作
            log.info("退款:{}", order.getAmount());

            Orders refund = Orders.builder()
                    .payStatus(Orders.REFUND)
                    .id(ordersRejectionDTO.getId())
                    .build();
            ordersMapper.update(refund);
        }
    }

    /**
     * 统计订单数目
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        //待接单
        Integer toBeConfirmedNum = ordersMapper.count(Orders.TO_BE_CONFIRMED);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedNum);
        //待派送
        Integer confirmed = ordersMapper.count(Orders.CONFIRMED);
        orderStatisticsVO.setConfirmed(confirmed);
        //已派送
        Integer deliveryInProgress = ordersMapper.count(Orders.DELIVERY_IN_PROGRESS);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param id
     */
    @Override
    public void confirm(Long id) {
        //判断订单是否存在
        boolean exist = orderExists(Orders.builder().id(id).build());
        if (!exist){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //接单前的状态需要为已支付
        Orders orders = ordersMapper.pageQuery(Orders.builder().id(id).build()).getResult().get(0);
        if (orders.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //接单
        Orders confirm = Orders.builder().status(Orders.CONFIRMED).id(id).build();
        ordersMapper.update(confirm);
    }

    @Override
    public void delivery(Long id) {
        //判断订单是否存在
        boolean exist = orderExists(Orders.builder().id(id).build());
        if (!exist){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //派送前的状态需要为已接单
        Orders orders = ordersMapper.pageQuery(Orders.builder().id(id).build()).getResult().get(0);
        if (orders.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //派送
        Orders confirm = Orders.builder().status(Orders.DELIVERY_IN_PROGRESS).id(id).build();
        ordersMapper.update(confirm);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        //判断该订单是不是该用户的
        Page orderPage = ordersMapper.pageQuery(Orders.builder()
                .id(id)
                .userId(BaseContext.getCurrentId())
                //待接单
                .status(Orders.TO_BE_CONFIRMED)
                .build());
        //不是自己的订单 或 已接单
        if (orderPage.getResult().isEmpty()){
            //无权操作该订单
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }


        //socket通知前端
        Map map = new HashMap<>();
        //1 来单提醒 2 客户催单
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","");
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * orderList 转为 orderVOList
     * @param orders
     * @return
     */
    private List<OrderVO> toOrderVOList(List<Orders> orders) {
        //提取orderIds
        List<Long> orderIdList = new ArrayList<>();
        for (Orders order : orders){
            orderIdList.add(order.getId());
        }
        //订单菜品信息
        //获取orderDetailList
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderIds(orderIdList);
        Map<Long, List<OrderDetail>> orderDetailMap = new HashMap<>();
        for (OrderDetail orderDetail : orderDetailList){
            Long orderId = orderDetail.getOrderId();
            //订单号已存入map
            if (orderDetailMap.containsKey(orderId)){
                orderDetailMap.get(orderId).add(orderDetail);
            }else {
                //未存入map
                List<OrderDetail> orderDetails = new ArrayList<>();
                orderDetails.add(orderDetail);
                orderDetailMap.put(orderId, orderDetails);
            }
        }

        //将ordersList转为 orderVOList
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders order : orders){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            //订单详细
            orderVO.setOrderDetailList(orderDetailMap.get(order.getId()));
            //菜品信息字符串
            orderVO.setOrderDishes(toOrderDishesStr(orderDetailMap.get(order.getId())));
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    /**
     * 转为菜品信息字符串
     * @param orderDetailList
     * @return
     */
    private String toOrderDishesStr(List<OrderDetail> orderDetailList) {
        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }
}
