package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyTask {

    /**
     * 我的任务测试
     */
//    @Scheduled(cron = "0/5 * * * * ?")
    public void myTask() {
        System.out.println("task test!" + LocalDateTime.now());
    }
}
