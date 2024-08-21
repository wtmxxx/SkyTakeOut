package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Autowired
    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);

            turnover = turnover == null ? 0.0 : turnover;

            turnoverList.add(turnover);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 每天新增用户数量
        List<Long> newUserList = new ArrayList<>();
        // 每天总用户数量
        List<Long> totalUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .ge(User::getCreateTime, LocalDateTime.of(localDate, LocalTime.MIN))
                    .le(User::getCreateTime, LocalDateTime.of(localDate, LocalTime.MAX));
            newUserList.add(userMapper.selectCount(lambdaQueryWrapper));

            lambdaQueryWrapper.clear();
            lambdaQueryWrapper.le(User::getCreateTime, LocalDateTime.of(localDate, LocalTime.MAX));
            totalUserList.add(userMapper.selectCount(lambdaQueryWrapper));
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Long> totalOrdersList = new ArrayList<>();
        List<Long> validOrdersList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .ge(Orders::getOrderTime, LocalDateTime.of(date, LocalTime.MIN))
                    .le(Orders::getOrderTime, LocalDateTime.of(date, LocalTime.MAX));
            totalOrdersList.add(orderMapper.selectCount(lambdaQueryWrapper));

            lambdaQueryWrapper.eq(Orders::getStatus, Orders.COMPLETED);
            validOrdersList.add(orderMapper.selectCount(lambdaQueryWrapper));
        }

        Long totalOrderCount = totalOrdersList.stream().reduce(0L, Long::sum);
        Long validOrderCount = validOrdersList.stream().reduce(0L, Long::sum);

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrdersList, ","))
                .validOrderCountList(StringUtils.join(validOrdersList, ","))
                .totalOrderCount(Math.toIntExact(totalOrderCount))
                .validOrderCount(Math.toIntExact(validOrderCount))
                .orderCompletionRate(totalOrderCount == 0L ? 0.0 : ((double) validOrderCount / (double) totalOrderCount))
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        System.out.println(beginTime + " " + endTime);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        String goodsName = goodsSalesDTOList
                .stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));
        String goodsNum = goodsSalesDTOList
                .stream()
                .map(obj -> obj.getNumber().toString())
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO
                .builder()
                .nameList(goodsName)
                .numberList(goodsNum)
                .build();
    }
}
