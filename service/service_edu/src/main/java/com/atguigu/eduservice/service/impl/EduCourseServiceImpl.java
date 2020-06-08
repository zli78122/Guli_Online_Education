package com.atguigu.eduservice.service.impl;

import com.atguigu.eduservice.entity.EduCourse;
import com.atguigu.eduservice.entity.EduCourseDescription;
import com.atguigu.eduservice.entity.EduSubject;
import com.atguigu.eduservice.entity.vo.CourseInfoVo;
import com.atguigu.eduservice.mapper.EduCourseMapper;
import com.atguigu.eduservice.service.EduCourseDescriptionService;
import com.atguigu.eduservice.service.EduCourseService;
import com.atguigu.eduservice.service.EduSubjectService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程 服务实现类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
@Service
public class EduCourseServiceImpl extends ServiceImpl<EduCourseMapper, EduCourse> implements EduCourseService {

    @Autowired
    private EduCourseDescriptionService courseDescriptionService;

    @Autowired
    private EduSubjectService subjectService;

    // 添加课程基本信息
    @Override
    public String saveCourseInfo(CourseInfoVo courseInfoVo) {
        //  1.向课程表添加课程基本信息
        // 属性复制: courseInfoVo -> eduCourse
        EduCourse eduCourse = new EduCourse();
        BeanUtils.copyProperties(courseInfoVo, eduCourse);

        // 获取 subject_parent_id
        String subjectId = eduCourse.getSubjectId();
        EduSubject eduSubject = subjectService.getById(subjectId);
        String parentId = eduSubject.getParentId();
        // 设置 subject_parent_id
        eduCourse.setSubjectParentId(parentId);

        int insert = baseMapper.insert(eduCourse);
        if(insert == 0) {
            // 添加失败
            throw new GuliException(20001, "添加课程信息失败");
        }

        //  获取添加之后的课程id
        String cid = eduCourse.getId();

        //  2.向课程简介表添加课程简介信息
        EduCourseDescription courseDescription = new EduCourseDescription();
        //  设置课程描述
        courseDescription.setDescription(courseInfoVo.getDescription());
        //  设置课程简介id就是课程id
        courseDescription.setId(cid);
        courseDescriptionService.save(courseDescription);

        return cid;
    }
}
