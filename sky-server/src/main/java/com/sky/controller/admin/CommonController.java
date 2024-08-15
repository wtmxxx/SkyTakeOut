package com.sky.controller.admin;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.sky.result.Result;
import com.sky.utils.LeanCloudUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@RequestMapping("/admin/common")
@Tag(name = "通用接口")
@ApiSupport(author = "Wotemo")
public class CommonController {
    private final LeanCloudUtil leanCloudUtil;
    @Autowired
    public CommonController(LeanCloudUtil leanCloudUtil) {
        this.leanCloudUtil = leanCloudUtil;
    }

    @PostMapping("/upload")
    @Operation(summary = "文件上传", description = "使用LeanCloud进行文件上传")
    @Parameter(name = "file", description = "文件", required = true, in = ParameterIn.DEFAULT)
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("文件上传: {}", file.getOriginalFilename());

        CompletableFuture<String> completableFuture = leanCloudUtil.upload(file);
        completableFuture.get();

        String fileUrl = completableFuture.join();

        log.info("文件链接: {}", fileUrl);

        return Result.success(fileUrl);
    }

}
