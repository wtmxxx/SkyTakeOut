package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.tapsdk.lc.json.JSON;
import org.apache.poi.ss.formula.ptg.MemAreaPtg;
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

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final UserMapper userMapper;
    private final WeChatPayUtil weChatPayUtil;

    @Autowired
    public OrderServiceImpl(AddressBookMapper addressBookMapper, ShoppingCartMapper shoppingCartMapper, OrderMapper orderMapper, OrderDetailMapper orderDetailMapper, UserMapper userMapper, WeChatPayUtil weChatPayUtil) {
        this.addressBookMapper = addressBookMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.userMapper = userMapper;
        this.weChatPayUtil = weChatPayUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(lambdaQueryWrapper);

        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insertOrder(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        Db.saveBatch(orderDetailList);

        LambdaUpdateWrapper<ShoppingCart> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartMapper.delete(lambdaQueryWrapper);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public void payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Orders order = new Orders();
        order.setNumber(ordersPaymentDTO.getOrderNumber());
        List<Orders> orderList = orderMapper
                .selectList(
                        new LambdaQueryWrapper<Orders>()
                                .eq(Orders::getNumber, ordersPaymentDTO.getOrderNumber()));
        if (orderList != null && orderList.size() == 1) {
            order = orderList.get(0);
            if (order.getPayStatus().equals(Orders.PAID)) {
                throw new OrderBusinessException("订单已支付");
            }
        } else {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }


    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

//        // 根据订单号查询订单
//        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
//
//        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
//        Orders orders = Orders.builder()
//                .id(ordersDB.getId())
//                .status(Orders.TO_BE_CONFIRMED)
//                .payStatus(Orders.PAID)
//                .checkoutTime(LocalDateTime.now())
//                .build();
//
//        orderMapper.updateOrder(orders);

        Orders order = Orders.builder()
                .number(outTradeNo).build();

        List<Orders> orderList = orderMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, order.getNumber()));
        if (orderList != null && orderList.size() == 1) {
            order = orderList.get(0);
            order.setCheckoutTime(LocalDateTime.now());
            order.setPayStatus(Orders.PAID);
            order.setStatus(Orders.TO_BE_CONFIRMED);
            orderMapper.updateById(order);
        }

        //TODO 接单提醒
//        Map map = new HashMap();
//        map.put("type", 1);
//        map.put("orderId", order.getId());
//        map.put("content", "订单号: " + outTradeNo);
//        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
