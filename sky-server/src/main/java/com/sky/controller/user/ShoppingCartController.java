package com.sky.controller.user;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@ApiSupport(author = "Wotemo")
@Tag(name = "C端购物车相关接口")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车, 商品信息为: {}", shoppingCartDTO);

        shoppingCartService.addShoppingCart(shoppingCartDTO);

        return Result.success();
    }
}
