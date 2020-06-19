package com.atguigu.staservice.service.impl;

import com.atguigu.commonutils.R;
import com.atguigu.staservice.client.UcenterClient;
import com.atguigu.staservice.entity.StatisticsDaily;
import com.atguigu.staservice.mapper.StatisticsDailyMapper;
import com.atguigu.staservice.service.StatisticsDailyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 网站统计日数据 服务实现类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-18
 */
@Service
public class StatisticsDailyServiceImpl extends ServiceImpl<StatisticsDailyMapper, StatisticsDaily> implements StatisticsDailyService {

    @Autowired
    private UcenterClient ucenterClient;

    // 统计某一天的注册人数，生成统计数据
    @Override
    public void registerCount(String day) {
        // 保存统计数据之前先删除相同日期的数据
        QueryWrapper<StatisticsDaily> wrapper = new QueryWrapper<>();
        wrapper.eq("date_calculated", day);
        baseMapper.delete(wrapper);

        // 查询某一天的注册人数
        R registerR = ucenterClient.countRegister(day);
        Integer countRegister = (Integer) registerR.getData().get("countRegister");

        // 生成统计数据
        StatisticsDaily sta = new StatisticsDaily();
        // 统计日期
        sta.setDateCalculated(day);
        // 注册人数
        sta.setRegisterNum(countRegister);
        // 登录人数
        sta.setLoginNum(RandomUtils.nextInt(100, 200));
        // 每日新增课程数
        sta.setCourseNum(RandomUtils.nextInt(100, 200));
        // 每日播放视频数
        sta.setVideoViewNum(RandomUtils.nextInt(100, 200));
        // 保存统计数据
        baseMapper.insert(sta);
    }

    // 获取统计数据
    @Override
    public Map<String, Object> getShowData(String type, String begin, String end) {
        // 条件查询统计数据
        QueryWrapper<StatisticsDaily> wrapper = new QueryWrapper<>();
        wrapper.between("date_calculated", begin, end);
        wrapper.select("date_calculated", type);
        List<StatisticsDaily> staList = baseMapper.selectList(wrapper);

        // 日期集合
        List<String> date_calculatedList = new ArrayList<>();
        // 数据集合
        List<Integer> numDataList = new ArrayList<>();

        for (int i = 0; i < staList.size(); i++) {
            StatisticsDaily daily = staList.get(i);
            // 日期
            date_calculatedList.add(daily.getDateCalculated());
            // 数据
            switch (type) {
                case "login_num":
                    numDataList.add(daily.getLoginNum());
                    break;
                case "register_num":
                    numDataList.add(daily.getRegisterNum());
                    break;
                case "video_view_num":
                    numDataList.add(daily.getVideoViewNum());
                    break;
                case "course_num":
                    numDataList.add(daily.getCourseNum());
                    break;
                default:
                    break;
            }
        }

        // 封装最终返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("date_calculatedList", date_calculatedList);
        map.put("numDataList", numDataList);

        return map;
    }
}
