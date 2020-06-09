package com.atguigu.eduservice.controller;

import com.atguigu.commonutils.R;
import com.atguigu.eduservice.entity.chapter.ChapterVo;
import com.atguigu.eduservice.service.EduChapterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 课程 前端控制器
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
@Api(tags = {"课程章节管理"})
@RestController
@RequestMapping("/eduservice/chapter")
@CrossOrigin //解决跨域问题
public class EduChapterController {

    @Autowired
    private EduChapterService chapterService;

    // 根据课程id查询课程大纲，包括章节和小节
    @ApiOperation(value = "根据课程id查询课程大纲")
    @GetMapping("/getChapterVideo/{courseId}")
    public R getChapterVideo(@PathVariable String courseId) {
        List<ChapterVo> list = chapterService.getChapterVideoByCourseId(courseId);
        return R.ok().data("allChapterVideo", list);
    }

}
