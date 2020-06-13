package com.atguigu.eduservice.client;

import com.atguigu.commonutils.R;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 熔断器
 */
@Component
public class VodFileDegradeFeignClient implements VodClient {

    @Override
    public R deleteVideo(String id) {
        return R.error().message("删除视频失败");
    }

    @Override
    public R deleteBatch(List<String> videoIdList) {
        return R.error().message("批量删除视频失败");
    }
}
