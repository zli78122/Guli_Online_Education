package com.atguigu.eduservice.controller;

import com.atguigu.commonutils.R;
import com.atguigu.eduservice.entity.subject.OneSubject;
import com.atguigu.eduservice.service.EduSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 课程科目 前端控制器
 * </p>
 *
 * @author zli78122
 * @since 2020-06-07
 */
@Api(tags = {"课程分类管理"})
@RestController
@RequestMapping("/eduservice/subject")
@CrossOrigin //解决跨域问题
public class EduSubjectController {

    @Autowired
    private EduSubjectService subjectService;

    /**
     * 获取用户上传的Excel文件，把文件内容读取出来
     * @param file 用户上传的Excel文件
     */
    @ApiOperation(value = "添加课程分类")
    @PostMapping("/addSubject")
    public R addSubject(MultipartFile file) {
        subjectService.saveSubject(file, subjectService);
        return R.ok();
    }

    /**
     * 查询所有课程分类，返回树形结构，前端以树形结构进行显示
     */
    @ApiOperation(value = "查询所有课程分类")
    @GetMapping("/getAllSubject")
    public R getAllSubject() {
        // 集合的泛型类型是 OneSubject(一级分类)
        // OneSubject对象 的 children属性 保存了 当前一级分类下的所有二级分类
        List<OneSubject> list = subjectService.getAllOneTwoSubject();
        return R.ok().data("list", list);
    }
}
