package com.atguigu.eduservice.controller;

import com.atguigu.commonutils.R;
import com.atguigu.eduservice.entity.EduVideo;
import com.atguigu.eduservice.service.EduVideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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

    @ApiOperation(value = "添加小节")
    @PostMapping("/addVideo")
    public R addVideo(@RequestBody EduVideo eduVideo) {
        videoService.save(eduVideo);
        return R.ok();
    }

    @ApiOperation(value = "删除小节")
    @DeleteMapping("/{id}")
    public R deleteVideo(@PathVariable String id) {
        videoService.removeById(id);
        return R.ok();
    }
}
