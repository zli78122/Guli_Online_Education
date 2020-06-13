package com.atguigu.vod.service.impl;

import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadStreamRequest;
import com.aliyun.vod.upload.resp.UploadStreamResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.vod.model.v20170321.DeleteVideoRequest;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.atguigu.vod.utils.ConstantVodUtils;
import com.atguigu.vod.service.VodService;
import com.atguigu.vod.utils.InitVodClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
public class VodServiceImpl implements VodService {

    // 上传视频到阿里云
    @Override
    public String uploadVideo(MultipartFile file) {
        try {
            //  fileName：上传文件的原始名称
            String fileName = file.getOriginalFilename();
            //  title：上传到阿里云后 视频的名称
            String title = fileName.substring(0, fileName.lastIndexOf("."));
            //  inputStream：上传文件输入流
            InputStream inputStream = file.getInputStream();

            // 请求对象
            UploadStreamRequest request = new UploadStreamRequest(
                    ConstantVodUtils.ACCESS_KEY_ID,
                    ConstantVodUtils.ACCESS_KEY_SECRET,
                    title,
                    fileName,
                    inputStream);

            UploadVideoImpl uploader = new UploadVideoImpl();

            // 响应对象
            UploadStreamResponse response = uploader.uploadStream(request);

            // 返回视频id
            String videoId = response.getVideoId();
            return videoId;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据id批量删除存储在阿里云服务器上的视频文件
    @Override
    public void removeMoreAlyVideo(List<String> videoIdList) {
        try {
            // 初始化对象
            DefaultAcsClient client = InitVodClient.initVodClient(ConstantVodUtils.ACCESS_KEY_ID, ConstantVodUtils.ACCESS_KEY_SECRET);

            // 创建专门用于删除视频的请求对象
            DeleteVideoRequest request = new DeleteVideoRequest();

            // 把 [1, 2, 3] 转换成 "1,2,3"
            String videoIds = StringUtils.join(videoIdList.toArray(), ",");

            // 把视频id设置到请求对象中
            request.setVideoIds(videoIds);

            // 发送请求
            client.getAcsResponse(request);

        } catch(Exception e) {
            e.printStackTrace();
            throw new GuliException(20001, "删除视频失败");
        }
    }
}
