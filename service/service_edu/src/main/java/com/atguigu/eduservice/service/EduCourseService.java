package com.atguigu.eduservice.service;

import com.atguigu.eduservice.entity.EduCourse;
import com.atguigu.eduservice.entity.frontvo.CourseFrontVo;
import com.atguigu.eduservice.entity.frontvo.CourseWebVo;
import com.atguigu.eduservice.entity.vo.CourseInfoVo;
import com.atguigu.eduservice.entity.vo.CoursePublishVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 课程 服务类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
public interface EduCourseService extends IService<EduCourse> {

    // 添加课程基本信息
    String saveCourseInfo(CourseInfoVo courseInfoVo);

    // 根据课程id查询课程基本信息
    CourseInfoVo getCourseInfo(String courseId);

    // 修改课程信息
    void updateCourseInfo(CourseInfoVo courseInfoVo);

    // 根据课程id查询课程确认信息
    CoursePublishVo publishCourseInfo(String id);

    // 删除课程
    void removeCourse(String courseId);

    // 分页条件查询课程
    Map<String, Object> getCourseFrontList(Page<EduCourse> pageCourse, CourseFrontVo courseFrontVo);

    // 根据课程id查询课程信息
    CourseWebVo getBaseCourseInfo(String courseId);
}
