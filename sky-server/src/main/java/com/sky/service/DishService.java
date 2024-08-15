package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void saveWithFlavor(DishDTO dishDTO);

    void deleteBatch(List<Long> ids);

    void startOrStop(Integer status, Long id);

    DishVO getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    List<Dish> listByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
