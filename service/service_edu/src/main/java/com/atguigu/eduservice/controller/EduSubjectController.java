package com.atguigu.eduservice.controller;

import com.atguigu.commonutils.R;
import com.atguigu.eduservice.service.EduSubjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}

