package com.atguigu.msmservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.msmservice.constant.MsmConstant;
import com.atguigu.msmservice.service.MsmService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class MsmServiceImpl implements MsmService {

    // 发送短信
    @Override
    public boolean send(Map<String, Object> param, String phone) {
        if (StringUtils.isEmpty(phone)) return false;

        DefaultProfile profile = DefaultProfile.getProfile("default", MsmConstant.ACCESS_KEY_ID, MsmConstant.ACCESS_KEY_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);

        // 固定的参数
        CommonRequest request = new CommonRequest();
//        request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        // 发送相关的参数
        // 手机号
        request.putQueryParameter("PhoneNumbers", phone);
        // 签名名称
        request.putQueryParameter("SignName", MsmConstant.SIGN_NAME);
        // 模版CODE
        request.putQueryParameter("TemplateCode", MsmConstant.TEMPLATE_CODE);
        // 验证码数据，转换成json格式进行传递
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));

        try {
            // 最终发送
            CommonResponse response = client.getCommonResponse(request);
            boolean success = response.getHttpResponse().isSuccess();
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
