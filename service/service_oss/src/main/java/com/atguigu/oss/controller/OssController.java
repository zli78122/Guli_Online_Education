package com.atguigu.oss.controller;

import com.atguigu.commonutils.R;
import com.atguigu.oss.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = {"Aliyun OSS"})
@RestController
@RequestMapping("/eduoss/fileoss")
@CrossOrigin //解决跨域问题
public class OssController {

    @Autowired
    private OssService ossService;

    @ApiOperation(value = "上传课程封面")
    @PostMapping("/cover")
    public R uploadCourseCover(MultipartFile file) {
        //  获取上传文件 MultipartFile对象
        //  返回上传到oss的路径
        String url = ossService.uploadFile(file, "cover");
        return R.ok().data("url", url);
    }

    @ApiOperation(value = "上传头像")
    @PostMapping("/avatar")
    public R uploadAvatar(MultipartFile file) {
        //  获取上传文件 MultipartFile对象
        //  返回上传到oss的路径
        String url = ossService.uploadFile(file, "avatar");
        return R.ok().data("url", url);
    }
}
