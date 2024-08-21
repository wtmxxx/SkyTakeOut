package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
    /**
     * 批量插入购物车数据
     *
     * @param shoppingCartList
     */
    void myInsertBatch(List<ShoppingCart> shoppingCartList);
}
