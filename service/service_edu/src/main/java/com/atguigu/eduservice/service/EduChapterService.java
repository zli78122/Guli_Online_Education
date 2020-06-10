package com.atguigu.eduservice.service;

import com.atguigu.eduservice.entity.EduChapter;
import com.atguigu.eduservice.entity.chapter.ChapterVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程 服务类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
public interface EduChapterService extends IService<EduChapter> {

    // 根据课程id查询课程大纲，包括章节和小节
    List<ChapterVo> getChapterVideoByCourseId(String courseId);

    // 删除章节
    boolean deleteChapter(String chapterId);

    // 根据课程id删除课程章节
    void removeChapterByCourseId(String courseId);
}
