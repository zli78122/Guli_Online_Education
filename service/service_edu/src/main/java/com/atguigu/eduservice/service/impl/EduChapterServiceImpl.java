package com.atguigu.eduservice.service.impl;

import com.atguigu.eduservice.entity.EduChapter;
import com.atguigu.eduservice.entity.EduVideo;
import com.atguigu.eduservice.entity.chapter.ChapterVo;
import com.atguigu.eduservice.entity.chapter.VideoVo;
import com.atguigu.eduservice.mapper.EduChapterMapper;
import com.atguigu.eduservice.service.EduChapterService;
import com.atguigu.eduservice.service.EduVideoService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程 服务实现类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-08
 */
@Service
public class EduChapterServiceImpl extends ServiceImpl<EduChapterMapper, EduChapter> implements EduChapterService {

    @Autowired
    private EduVideoService videoService;

    // 根据课程id查询课程大纲，包括章节和小节
    @Override
    public List<ChapterVo> getChapterVideoByCourseId(String courseId) {
        //  1.根据课程id查询课程里面所有的章节
        QueryWrapper<EduChapter> wrapperChapter = new QueryWrapper<>();
        wrapperChapter.eq("course_id", courseId);
        List<EduChapter> eduChapterList = baseMapper.selectList(wrapperChapter);

        //  2.根据课程id查询课程里面所有的小节
        QueryWrapper<EduVideo> wrapperVideo = new QueryWrapper<>();
        wrapperVideo.eq("course_id", courseId);
        List<EduVideo> eduVideoList = videoService.list(wrapperVideo);

        //  创建list集合，用于最终封装数据
        List<ChapterVo> finalList = new ArrayList<>();

        //  3.遍历查询章节list集合进行封装
        //  遍历查询章节list集合
        for (int i = 0; i < eduChapterList.size(); i++) {
            //  每个章节
            EduChapter eduChapter = eduChapterList.get(i);
            //  eduChapter对象值复制到ChapterVo里面
            ChapterVo chapterVo = new ChapterVo();
            BeanUtils.copyProperties(eduChapter, chapterVo);
            //  把chapterVo放到最终list集合
            finalList.add(chapterVo);

            //  创建集合，用于封装章节的小节
            List<VideoVo> videoList = new ArrayList<>();

            //  4.遍历查询小节list集合，进行封装
            for (int m = 0; m < eduVideoList.size(); m++) {
                //  得到每个小节
                EduVideo eduVideo = eduVideoList.get(m);
                //  判断：小节里面chapterId和章节里面id是否一样
                if(eduVideo.getChapterId().equals(eduChapter.getId())) {
                    //  进行封装
                    VideoVo videoVo = new VideoVo();
                    BeanUtils.copyProperties(eduVideo, videoVo);
                    //  放到小节封装集合
                    videoList.add(videoVo);
                }
            }
            //  把封装之后小节list集合，放到章节对象里面
            chapterVo.setChildren(videoList);
        }
        return finalList;
    }

    // 删除章节
    @Override
    public boolean deleteChapter(String chapterId) {
        // 章节中没有小节 -> 删除
        // 章节中有小节 -> 不删除

        //  根据章节id查询小节，如果能查到数据，则不能删除章节
        QueryWrapper<EduVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId);
        int count = videoService.count(wrapper);
        if(count > 0) {
            //  查询出小节 -> 不能删除章节
            throw new GuliException(20001,"不能删除");
        } else {
            //  没有查询出小节 -> 删除章节
            int result = baseMapper.deleteById(chapterId);
            return result > 0;
        }
    }

    // 根据课程id删除课程章节
    @Override
    public void removeChapterByCourseId(String courseId) {
        QueryWrapper<EduChapter> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        baseMapper.delete(wrapper);
    }
}
