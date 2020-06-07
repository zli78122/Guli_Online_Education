package com.atguigu.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.oss.service.OssService;
import com.atguigu.oss.utils.ConstantPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    // 上传头像到oss
    @Override
    public String uploadFileAvatar(MultipartFile file) {
        // 获取oss连接参数
        String endpoint = ConstantPropertiesUtils.END_POIND;
        String accessKeyId = ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        String bucketName = ConstantPropertiesUtils.BUCKET_NAME;

        try {
            // 创建OSS实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 获取上传文件输入流
            InputStream inputStream = file.getInputStream();
            // 获取文件名称
            // 1.jpg
            String fileName = file.getOriginalFilename();

            // 在文件名称里面添加随机唯一的值
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            // 027ae1a3bee04da693ec1116307ffb55-1.jpg
            fileName = uuid + "-" + fileName;

            // 把文件按照日期进行分类
            // 获取当前日期
            String datePath = new DateTime().toString("yyyyMMdd");
            // 拼接日期
            // 20200607/027ae1a3bee04da693ec1116307ffb55-1.jpg
            fileName = datePath + "/" + fileName;

            // 添加 'avatar/' 前缀
            // avatar/20200607/027ae1a3bee04da693ec1116307ffb55-1.jpg
            fileName = "avatar/" + fileName;

            // 调用oss方法实现上传
            //   第一个参数: Bucket名称
            //   第二个参数: 上传到oss文件路径和文件名称
            //   第三个参数: 上传文件输入流
            ossClient.putObject(bucketName, fileName , inputStream);

            // 关闭OSSClient
            ossClient.shutdown();

            // 把上传之后文件的路径返回
            // 需要把上传到阿里云oss的路径手动拼接出来
            // https://zli78122-guli-edu.oss-us-west-1.aliyuncs.com/avatar/20200607/027ae1a3bee04da693ec1116307ffb55-1.jpg
            String url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            return url;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
