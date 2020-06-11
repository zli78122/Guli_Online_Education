package com.atguigu.vod.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 使用 @Value注解 读取 application.properties 里的配置内容。
 * 用Spring的 InitializingBean接口 的 afterPropertiesSet()方法 来初始化配置信息，这个方法将在 所有的属性被初始化后 调用。
 */
@Component
public class ConstantVodUtils implements InitializingBean {

    // 读取配置文件内容
    @Value("${aliyun.vod.file.keyid}")
    private String keyid;

    @Value("${aliyun.vod.file.keysecret}")
    private String keysecret;

    // 定义公开静态常量
    public static String ACCESS_KEY_SECRET;
    public static String ACCESS_KEY_ID;

    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_KEY_ID = keyid;
        ACCESS_KEY_SECRET = keysecret;
    }
}
