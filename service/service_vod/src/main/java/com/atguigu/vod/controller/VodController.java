package com.atguigu.vod.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.vod.model.v20170321.DeleteVideoRequest;
import com.atguigu.commonutils.R;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.atguigu.vod.service.VodService;
import com.atguigu.vod.utils.ConstantVodUtils;
import com.atguigu.vod.utils.InitVodClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Api(tags = {"Aliyun 视频点播"})
@RestController
@RequestMapping("/eduvod/video")
@CrossOrigin
public class VodController {

    @Autowired
    private VodService vodService;

    @ApiOperation(value = "根据id批量删除存储在阿里云服务器上的视频文件")
    @DeleteMapping("/delete-batch")
    public R deleteBatch(@RequestParam("videoIdList") List<String> videoIdList) {
        vodService.removeMoreAlyVideo(videoIdList);
        return R.ok();
    }

    @ApiOperation(value = "根据id删除存储在阿里云服务器上的视频文件")
    @DeleteMapping("/deleteVideo/{id}")
    public R deleteVideo(@PathVariable String id) {
        try {
            // 初始化对象
            DefaultAcsClient client = InitVodClient.initVodClient(ConstantVodUtils.ACCESS_KEY_ID, ConstantVodUtils.ACCESS_KEY_SECRET);

            // 创建专门用于删除视频的请求对象
            DeleteVideoRequest request = new DeleteVideoRequest();

            // 把视频id设置到请求对象中
            request.setVideoIds(id);

            // 发送请求
            client.getAcsResponse(request);

            return R.ok();
        }catch(Exception e) {
            e.printStackTrace();
            throw new GuliException(20001, "删除视频失败");
        }
    }

    @ApiOperation(value = "上传视频到阿里云")
    @PostMapping("/uploadVideo")
    public R uploadVideo(MultipartFile file) {
        // 返回视频id
        String videoId = vodService.uploadVideo(file);
        return R.ok().data("videoId", videoId);
    }
}
