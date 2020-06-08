package com.atguigu.eduservice.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.eduservice.entity.EduSubject;
import com.atguigu.eduservice.entity.excel.SubjectData;
import com.atguigu.eduservice.service.EduSubjectService;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 因为 SubjectExcelListener 不能交给Spring进行管理，需要我们自己手动去 new SubjectExcelListener对象
 * 所以不能在这个类中注入其他Spring管理的对象，比如 Mapper、Service
 * 因此，在这个类中不能实现对数据库的操作 (准确来说，是不能通过 Service、Mapper 来实现对数据库的操作
 * 如果我们手动写JDBC代码，也可以实现对数据库的操作，但那样就太麻烦了)
 *
 * 解决方案其实很简单：new SubjectExcelListener对象时，通过构造器传参的形式把 EduSubjectService对象 注入进来
 *
 * 结论：不能使用 @Autowired注解 把Service对象注入进来，而应该通过构造器传参的形式把 EduSubjectService对象 注入进来
 */
public class SubjectExcelListener extends AnalysisEventListener<SubjectData> {

    private  EduSubjectService subjectService;

    public SubjectExcelListener() {

    }

    public SubjectExcelListener(EduSubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // 一行一行读取Excel内容
    @Override
    public void invoke(SubjectData subjectData, AnalysisContext analysisContext) {
        if(subjectData == null) {
            throw new GuliException(20001, "文件数据为空");
        }

        // 一行一行读取，每行有两个值，第一个值是一级分类，第二个值是二级分类

        // 添加一级分类
        // 同名的一级分类不能重复添加，需要做判断
        EduSubject existOneSubject = this.existOneSubject(subjectService, subjectData.getOneSubjectName());
        if(existOneSubject == null) {
            // 数据库中不存在同名的一级分类，可以添加
            existOneSubject = new EduSubject();
            existOneSubject.setParentId("0");
            existOneSubject.setTitle(subjectData.getOneSubjectName()); //一级分类名称
            subjectService.save(existOneSubject);
        }

        // 获取一级分类的id值，也是二级分类的pid值
        String pid = existOneSubject.getId();

        // 添加二级分类
        // 同名的二级分类不能重复添加，需要做判断
        EduSubject existTwoSubject = this.existTwoSubject(subjectService, subjectData.getTwoSubjectName(), pid);
        if(existTwoSubject == null) {
            // 数据库中不存在同名的二级分类，可以添加
            existTwoSubject = new EduSubject();
            existTwoSubject.setParentId(pid);
            existTwoSubject.setTitle(subjectData.getTwoSubjectName()); //二级分类名称
            subjectService.save(existTwoSubject);
        }
    }

    // 同名的一级分类不能重复添加，需要做判断
    private EduSubject existOneSubject(EduSubjectService subjectService, String name) {
        QueryWrapper<EduSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("title", name);
        wrapper.eq("parent_id", "0");
        EduSubject oneSubject = subjectService.getOne(wrapper);
        return oneSubject;
    }

    // 同名的二级分类不能重复添加，需要做判断
    private EduSubject existTwoSubject(EduSubjectService subjectService, String name, String pid) {
        QueryWrapper<EduSubject> wrapper = new QueryWrapper<>();
        wrapper.eq("title", name);
        wrapper.eq("parent_id", pid);
        EduSubject twoSubject = subjectService.getOne(wrapper);
        return twoSubject;
    }

    // 读取完成之后
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
