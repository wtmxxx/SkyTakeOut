package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import java.util.Base64;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
