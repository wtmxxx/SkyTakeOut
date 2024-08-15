package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Tag(name = "菜品相关接口")
public class DishController {
    private final DishService dishService;
    private final RedisTemplate<String, List<DishVO>> redisTemplate;
    @Autowired
    public DishController(DishService dishService, RedisTemplate<String, List<DishVO>> redisTemplate) {
        this.dishService = dishService;
        this.redisTemplate = redisTemplate;
    }

    private void deleteKey(Long categoryId) {
        String key = "dish::" + categoryId;
        log.info("清除Redis缓存: {}", key);
        redisTemplate.delete(key);
    }

    private void deleteAll() {
        log.info("清除所有Redis分类菜品缓存");
        Set<String> keys = redisTemplate.keys("dish_*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @Operation(summary = "新增菜品", description = "新增菜品")
    @Parameter(name = "dishDTO", description = "菜品DTO", required = true, in = ParameterIn.DEFAULT)
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品: {}", dishDTO);
        deleteKey(dishDTO.getCategoryId());

        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "菜品分页查询", description = "菜品分页查询")
    @Parameter(name = "dishPageQueryDTO", description = "菜品分页查询DTO", required = true, in = ParameterIn.DEFAULT)
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @Operation(summary = "批量删除菜品", description = "批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品: {}", ids);
        deleteAll();

        dishService.deleteBatch(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启售（停售）菜品", description = "设置菜品的启售状态")
    @Parameters({
            @Parameter(name = "status", description = "状态码", example = "0", required = true, in = ParameterIn.PATH),
            @Parameter(name = "id", description = "菜品ID", example = "1", required = true, in = ParameterIn.DEFAULT),
    })
    public Result startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("启售（停售）菜品, id: {}, status: {}", id, status);
        deleteAll();

        dishService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜品", description = "根据ID查询菜品")
    @Parameter(name = "id", description = "菜品ID", example = "76", required = true, in = ParameterIn.PATH)
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品, id: {}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @Operation(summary = "修改菜品", description = "修改菜品")
    @Parameter(name = "dishDTO", description = "菜品DTO", required = true, in = ParameterIn.DEFAULT)
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品, dishDTO: {}", dishDTO);
        deleteAll();

        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.listByCategoryId(categoryId);
        return Result.success(list);
    }

}
