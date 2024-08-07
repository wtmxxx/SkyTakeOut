package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Tag(name = "员工相关接口")
@ApiSupport(author = "Wotemo")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Operation(summary = "员工登录", description = "以员工身份登录")
    @Parameters({@Parameter(name = "employeeLoginDTO", description = "员工登录DTO", required = true, in = ParameterIn.DEFAULT)})
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return Result
     */
    @Operation(summary = "员工登出", description = "以员工身份登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return Result
     */
    @PostMapping
    @Operation(summary = "新增员工", description = "用某个员工账号新增员工")
    @ApiResponse(responseCode = "1", description = "保存成功")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        employeeService.save(employeeDTO);
        log.info("新增员工: {}", employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询", description = "员工分页查询")
    @ApiResponse(responseCode = "1", description = "查询成功")
    @Parameters({
            @Parameter(name = "name", description = "员工姓名", example = "沃", required = true, in = ParameterIn.DEFAULT),
            @Parameter(name = "page", description = "第几页", example = "1", required = true, in = ParameterIn.DEFAULT),
            @Parameter(name = "pageSize", description = "每页几条", example = "10", required = true, in = ParameterIn.DEFAULT)
    })
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询, 参数为: {}", employeePageQueryDTO);
//        employeePageQueryDTO.setPage(employeePageQueryDTO.getPage()-1);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用（禁用）员工账号", description = "设置员工账号的可用性")
    @Parameters({
            @Parameter(name = "status", description = "状态码", example = "0", required = true, in = ParameterIn.PATH),
            @Parameter(name = "id", description = "员工ID", example = "1", required = true, in = ParameterIn.DEFAULT),
    })
    public Result startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("启用（禁用）员工账号, id: {}, status: {}", id, status);

        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "id查询员工", description = "通过ID查询员工信息")
    @Parameters({
            @Parameter(name = "id", description = "员工ID", example = "1", required = true, in = ParameterIn.PATH),
    })
    public Result<Employee> getById(@PathVariable("id") Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    @Operation(summary = "编辑员工信息", description = "编辑员工信息")
    @Parameter(name = "employeeDTO", description = "员工DTO", required = true, in = ParameterIn.DEFAULT)
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工信息");
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
