package com.atguigu.eduservice.client;

import com.atguigu.commonutils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// name = "service-vod" : 远程调用 service-vod 微服务
// fallback = VodFileDegradeFeignClient.class : 服务熔断机制
@FeignClient(name = "service-vod", fallback = VodFileDegradeFeignClient.class)
@Component
public interface VodClient {

    // 根据视频id删除存储在阿里云服务器上的视频文件
    @DeleteMapping("/eduvod/video/deleteVideo/{id}")
    R deleteVideo(@PathVariable String id);

    // 根据视频id批量删除存储在阿里云服务器上的视频文件
    @DeleteMapping("/eduvod/video/delete-batch")
    R deleteBatch(@RequestParam("videoIdList") List<String> videoIdList);
}
