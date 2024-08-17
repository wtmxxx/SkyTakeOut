package com.sky.controller.user;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/order")
@Slf4j
@ApiSupport(author = "Wotemo")
@Tag(name = "用户端订单相关接口")
public class OrderController {
    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/submit")
    @Operation(summary = "用户下单", description = "用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单, 参数为: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @Operation(summary = "订单支付", description = "订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        orderService.payment(ordersPaymentDTO);
//        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success();
    }

    @PutMapping("/paySuccess")
    @Operation(summary = "支付成功", description = "支付成功")
    public Result paySuccess(@RequestBody String orderNumber) {
        log.info("订单支付成功: {}", orderNumber);
        orderService.paySuccess(orderNumber);
        return Result.success();
    }
}
