package com.sky.controller.user;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("UserShopController")
@RequestMapping("/user/shop")
@Slf4j
@ApiSupport(author = "Wotemo")
@Tag(name = "店铺相关接口")
public class ShopController {
    public static final String SHOP_STATUS_KEY = "SHOP_STATUS";

    private final RedisTemplate<String, Short> redisTemplate;
    @Autowired
    public ShopController(RedisTemplate<String, Short> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取店铺的营业状态
     * @return
     */
    @GetMapping("/status")
    @Operation(summary = "获取营业状态", description = "获取店铺的营业状态, 1: 营业, 0: 打烊")
    public Result getStatus() {
        ValueOperations<String, Short> ops = redisTemplate.opsForValue();
        Short status = ops.get(SHOP_STATUS_KEY);
        log.info("获取店铺的营业状态为: {}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
