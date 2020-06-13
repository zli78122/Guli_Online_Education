package com.atguigu.eduservice.service.impl;

import com.atguigu.eduservice.client.VodClient;
import com.atguigu.eduservice.entity.EduVideo;
import com.atguigu.eduservice.mapper.EduVideoMapper;
import com.atguigu.eduservice.service.EduVideoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程视频 服务实现类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
@Service
public class EduVideoServiceImpl extends ServiceImpl<EduVideoMapper, EduVideo> implements EduVideoService {

    @Autowired
    private VodClient vodClient;

    // 根据课程id删除课程小节 (删除小节，同时删除视频)
    @Override
    public void removeVideoByCourseId(String courseId) {
        // 根据课程id查询当前课程的所有视频id
        QueryWrapper<EduVideo> wrapperVideo = new QueryWrapper<>();
        wrapperVideo.eq("course_id", courseId);
        wrapperVideo.select("video_source_id");
        List<EduVideo> eduVideoList = baseMapper.selectList(wrapperVideo);

        // 把 List<EduVideo> 转变成 List<String>
        List<String> videoIds = new ArrayList<>();
        for (int i = 0; i < eduVideoList.size(); i++) {
            // 小节对象
            EduVideo eduVideo = eduVideoList.get(i);
            // 视频id
            String videoSourceId = eduVideo.getVideoSourceId();
            if(!StringUtils.isEmpty(videoSourceId)) {
                videoIds.add(videoSourceId);
            }
        }

        // 批量删除视频
        if (videoIds.size() > 0) {
            vodClient.deleteBatch(videoIds);
        }

        // 删除小节
        QueryWrapper<EduVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        baseMapper.delete(wrapper);
    }
}
