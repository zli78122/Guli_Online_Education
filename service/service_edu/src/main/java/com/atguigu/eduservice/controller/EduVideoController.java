package com.atguigu.eduservice.controller;

import com.atguigu.commonutils.R;
import com.atguigu.eduservice.client.VodClient;
import com.atguigu.eduservice.entity.EduVideo;
import com.atguigu.eduservice.service.EduVideoService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 课程视频 前端控制器
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
@Api(tags = {"课程小节管理"})
@RestController
@RequestMapping("/eduservice/video")
@CrossOrigin //解决跨域问题
public class EduVideoController {

    @Autowired
    private EduVideoService videoService;

    @Autowired
    private VodClient vodClient;

    @ApiOperation(value = "添加小节")
    @PostMapping("/addVideo")
    public R addVideo(@RequestBody EduVideo eduVideo) {
        videoService.save(eduVideo);
        return R.ok();
    }

    @ApiOperation(value = "删除小节，同时删除视频")
    @DeleteMapping("/{id}")
    public R deleteVideo(@PathVariable String id) {
        // 小节对象
        EduVideo eduVideo = videoService.getById(id);
        // 视频id
        String videoSourceId = eduVideo.getVideoSourceId();

        if(!StringUtils.isEmpty(videoSourceId)) {
            // 根据视频id删除存储在阿里云服务器上的视频文件
            R result = vodClient.deleteVideo(videoSourceId);
            if(result.getCode() == 20001) {
                throw new GuliException(20001, "删除视频失败，触发熔断器执行");
            }
        }

        // 删除小节
        videoService.removeById(id);

        return R.ok();
    }
}
