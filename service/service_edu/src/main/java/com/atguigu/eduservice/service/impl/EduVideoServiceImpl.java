package com.atguigu.eduservice.service.impl;

import com.atguigu.eduservice.entity.EduVideo;
import com.atguigu.eduservice.mapper.EduVideoMapper;
import com.atguigu.eduservice.service.EduVideoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

    // 根据课程id删除课程小节
    @Override
    public void removeVideoByCourseId(String courseId) {
        QueryWrapper<EduVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        baseMapper.delete(wrapper);
    }
}
