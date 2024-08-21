package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    private final OrderMapper orderMapper;
    @Autowired
    public OrderTask(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Scheduled(cron = "0 * * * * *")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单: {}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, time);

        List<Orders> orderList = orderMapper.selectList(lambdaQueryWrapper);

        if (orderList != null && !orderList.isEmpty()) {
            for (Orders order : orderList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.updateById(order);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理处于派送中的订单: {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .lt(Orders::getOrderTime, time);

        List<Orders> orderList = orderMapper.selectList(lambdaQueryWrapper);

        if (orderList != null && !orderList.isEmpty()) {
            for (Orders order : orderList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.updateById(order);
            }
        }
    }
}
