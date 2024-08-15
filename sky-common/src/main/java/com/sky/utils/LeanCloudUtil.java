package com.sky.utils;

import com.tapsdk.lc.LCFile;
import com.tapsdk.lc.LCLogger;
import com.tapsdk.lc.core.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public class LeanCloudUtil {
    private final String errorMessage = "文件上传失败，可能是文件无法被读取，或者上传过程中出现问题";

    public LeanCloudUtil(String appId, String appKey, String serverUrl){
        LeanCloud.initialize(appId, appKey, serverUrl);
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
    }
    @Async
    public CompletableFuture<String> upload(MultipartFile file) throws Exception {
        CompletableFuture<String> stringCompletableFuture = new CompletableFuture<>();
        // 获取文件字节码
        byte[] fileBytes = file.getBytes();
        // 生成随机文件名
        String OriginalFilename = file.getOriginalFilename();
        int extIndex;
        if (OriginalFilename != null) {
            extIndex = OriginalFilename.lastIndexOf(".");
        } else {
            throw new Exception(errorMessage);
        }
        UUID uuid = UUID.randomUUID();
        String filename;
        filename = uuid + OriginalFilename.substring(extIndex);
        // 创建LeanCloud文件
        LCFile LCfile = new LCFile(filename, fileBytes);
        LCfile.saveInBackground().subscribe(new Observer<>() {
            public void onSubscribe(Disposable disposable) {
            }

            public void onNext(LCFile file) {
                log.info("文件保存完成 URL: {}", file.getUrl());
                stringCompletableFuture.complete(file.getUrl());
            }

            public void onError(Throwable throwable) {
                // 保存失败，可能是文件无法被读取，或者上传过程中出现问题
                try {
                    throw new Exception(errorMessage);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public void onComplete() {
            }
        });

        return stringCompletableFuture;
    }

}

