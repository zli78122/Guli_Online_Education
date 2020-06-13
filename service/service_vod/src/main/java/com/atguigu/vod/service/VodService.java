package com.atguigu.vod.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VodService {

    // 上传视频到阿里云
    String uploadVideo(MultipartFile file);

    // 根据id批量删除存储在阿里云服务器上的视频文件
    void removeMoreAlyVideo(List<String> videoIdList);
}
