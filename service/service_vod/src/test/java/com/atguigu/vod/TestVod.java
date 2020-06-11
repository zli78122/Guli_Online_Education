package com.atguigu.vod;

import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadVideoRequest;
import com.aliyun.vod.upload.resp.UploadVideoResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import org.junit.Test;

import java.util.List;

public class TestVod {

    /**
     * 上传视频
     */
    @Test
    public void test3() {
        // 即将要存储在阿里云的 视频名称
        String title = "What If I Want to Move Faster.mp4";
        // 本地文件路径
        String fileName = "/Users/zhengyuli/Desktop/What If I Want to Move Faster.mp4";

        // 请求对象
        UploadVideoRequest request = new UploadVideoRequest(VodConstant.ACCESS_KEY_ID, VodConstant.ACCESS_KEY_SECRET, title, fileName);
        /* 分片上传时每个分片的大小，默认为2M字节 */
        request.setPartSize(2 * 1024 * 1024L);
        /* 分片上传时的并发线程数，默认为1 */
        request.setTaskNum(1);

        UploadVideoImpl uploader = new UploadVideoImpl();

        // 响应对象
        UploadVideoResponse response = uploader.uploadVideo(request);

        if (response.isSuccess()) {
            System.out.println("VideoId = " + response.getVideoId());
        } else {
            /* 如果设置回调URL无效，不影响视频上传，可以返回VideoId同时会返回错误码。其他情况上传失败时，VideoId为空，此时需要根据返回错误码分析具体错误原因 */
            System.out.println("VideoId = " + response.getVideoId());
            System.out.println("ErrorCode = " + response.getCode());
            System.out.println("ErrorMessage = " + response.getMessage());
        }
    }

    /**
     * 根据视频ID获取视频播放凭证
     */
    @Test
    public void test2() throws Exception {
        // 初始化对象
        DefaultAcsClient client = InitObject.initVodClient(VodConstant.ACCESS_KEY_ID, VodConstant.ACCESS_KEY_SECRET);

        // 请求对象
        GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();

        // 把视频id设置到请求对象中
        request.setVideoId(VodConstant.VIDEO_ID);

        // 响应对象
        GetVideoPlayAuthResponse response = client.getAcsResponse(request);

        // 视频播放凭证
        System.out.println("playAuth = " + response.getPlayAuth());
    }

    /**
     * 根据视频ID获取视频播放地址
     */
    @Test
    public void test() throws Exception {
        // 初始化对象
        DefaultAcsClient client = InitObject.initVodClient(VodConstant.ACCESS_KEY_ID, VodConstant.ACCESS_KEY_SECRET);

        // 请求对象
        GetPlayInfoRequest request = new GetPlayInfoRequest();

        // 把视频id设置到请求对象中
        request.setVideoId(VodConstant.VIDEO_ID);

        // 响应对象
        GetPlayInfoResponse response = client.getAcsResponse(request);

        List<GetPlayInfoResponse.PlayInfo> playInfoList = response.getPlayInfoList();
        for (GetPlayInfoResponse.PlayInfo playInfo : playInfoList) {
            // 视频播放地址
            System.out.println("PlayInfo.PlayURL = " + playInfo.getPlayURL());
        }
        // 视频名称
        System.out.println("VideoBase.Title = " + response.getVideoBase().getTitle());
    }
}
