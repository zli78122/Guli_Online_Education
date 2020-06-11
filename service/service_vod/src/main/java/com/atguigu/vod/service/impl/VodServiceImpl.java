package com.atguigu.vod.service.impl;

import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadStreamRequest;
import com.aliyun.vod.upload.resp.UploadStreamResponse;
import com.atguigu.vod.utils.ConstantVodUtils;
import com.atguigu.vod.service.VodService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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
}
