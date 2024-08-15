package com.sky.controller.admin;

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

@RestController("AdminShopController")
@RequestMapping("/admin/shop")
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
     * 设置店铺的营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @Operation(summary = "修改营业状态", description = "设置店铺的营业状态, 1: 营业, 0: 打烊")
    @Parameter(name = "status", description = "营业状态", example = "1", required = true, in = ParameterIn.PATH)
    public Result setStatus(@PathVariable Short status) {
        log.info("设置店铺的营业状态为: {}", status == 1 ? "营业中" : "打烊中");
        ValueOperations<String, Short> ops = redisTemplate.opsForValue();
        ops.set(SHOP_STATUS_KEY, status);
        return Result.success();
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
