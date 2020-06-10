package com.atguigu.eduservice.mapper;

import com.atguigu.eduservice.entity.EduCourse;
import com.atguigu.eduservice.entity.vo.CoursePublishVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 课程 Mapper 接口
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
public interface EduCourseMapper extends BaseMapper<EduCourse> {

    // 根据课程id查询课程确认信息
    CoursePublishVo getPublishCourseInfo(String courseId);
}
