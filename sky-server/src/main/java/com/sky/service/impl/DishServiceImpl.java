package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final CategoryMapper categoryMapper;

    public DishServiceImpl(DishMapper dishMapper, DishFlavorMapper dishFlavorMapper, SetmealDishMapper setmealDishMapper, CategoryMapper categoryMapper) {
        this.dishMapper = dishMapper;
        this.dishFlavorMapper = dishFlavorMapper;
        this.setmealDishMapper = setmealDishMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        dishPageQueryDTO.setPage((dishPageQueryDTO.getPage()-1)*dishPageQueryDTO.getPageSize());
        List<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (dishPageQueryDTO.getName() != null) lambdaQueryWrapper.like(Dish::getName, dishPageQueryDTO.getName());
        if (dishPageQueryDTO.getCategoryId() != null) lambdaQueryWrapper.like(Dish::getCategoryId, dishPageQueryDTO.getCategoryId());
        if (dishPageQueryDTO.getStatus() != null) lambdaQueryWrapper.like(Dish::getStatus, dishPageQueryDTO.getStatus());

        Long countDish = dishMapper.selectCount(lambdaQueryWrapper);
//        log.info("countDish:{}", countDish);

        return PageResult.builder()
                .total(countDish)
                .records(page)
                .build();
    }

    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());

        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());

        dishMapper.insert(dish);

        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            Db.saveBatch(flavors);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getDishId, ids).select(SetmealDish::getSetmealId);
        List<Long> setmealIds = setmealDishMapper.selectObjs(lambdaQueryWrapper);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        dishMapper.deleteBatchIds(ids);
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(DishFlavor::getDishId, ids);
        dishFlavorMapper.delete(lambdaQueryWrapper1);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Dish> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper
                .eq(Dish::getId, id)
                .set(Dish::getStatus, status)
                .set(Dish::getUpdateTime, LocalDateTime.now())
                .set(Dish::getUpdateUser, BaseContext.getCurrentId());
        dishMapper.update(lambdaUpdateWrapper);
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.selectById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorMapper.selectList(lambdaQueryWrapper);
        dishVO.setFlavors(flavors);
        dishVO.setCategoryName(categoryMapper.getNameById(dish.getCategoryId()));
        return dishVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.updateById(dish);

        dishFlavorMapper.delete(
                new LambdaQueryWrapper<DishFlavor>()
                        .eq(DishFlavor::getDishId, dish.getId())
        );
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            Db.saveBatch(flavors);
        }
    }

    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.listByCategoryId(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus, dish.getStatus());
        List<Dish> dishList = dishMapper.selectList(lambdaQueryWrapper);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            Map<String, Object> map = new HashMap<>();
            map.put("dish_id" ,d.getId());
            List<DishFlavor> flavors = dishFlavorMapper.selectByMap(map);

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
