package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService extends IService<Dish> {
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void saveWithFlavor(DishDTO dishDTO);

    void deleteBatch(List<Long> ids);
}
