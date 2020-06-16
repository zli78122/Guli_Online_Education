package com.atguigu.educenter.controller;

import com.atguigu.commonutils.JwtUtils;
import com.atguigu.educenter.entity.UcenterMember;
import com.atguigu.educenter.service.UcenterMemberService;
import com.atguigu.educenter.utils.ConstantWxUtils;
import com.atguigu.educenter.utils.HttpClientUtils;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.util.HashMap;

@Api(tags = {"微信扫码登录"})
@Controller
@RequestMapping("/api/ucenter/wx")
@CrossOrigin
public class WxApiController {

    @Autowired
    private UcenterMemberService memberService;

    @ApiOperation(value = "获取扫码人微信信息，添加到数据库，并返回token值")
    @GetMapping("/callback")
    public String callback(String code, String state) {
        try {
            // 1.获取code值，临时票据，类似于验证码

            // 2.请求微信API，得到 access_token 和 openid
            // 请求地址
            String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                    "?appid=%s" +
                    "&secret=%s" +
                    "&code=%s" +
                    "&grant_type=authorization_code";

            // 填充 %s 占位符 (appid, appsecret, code)
            String accessTokenUrl = String.format(
                    baseAccessTokenUrl,
                    ConstantWxUtils.WX_OPEN_APP_ID,
                    ConstantWxUtils.WX_OPEN_APP_SECRET,
                    code
            );

            // 使用HttpClient发送请求，请求地址为 accessTokenUrl，得到返回结果 (返回结果中包含 access_token 和 openid)
            String accessTokenInfo = HttpClientUtils.get(accessTokenUrl);

            Gson gson = new Gson();
            // 把 accessTokenInfo 转换为 Map
            HashMap mapAccessToken = gson.fromJson(accessTokenInfo, HashMap.class);
            // 获取 access_token
            String access_token = (String)mapAccessToken.get("access_token");
            // 获取 openid
            String openid = (String)mapAccessToken.get("openid");

            // 3.根据openid查询用户信息
            UcenterMember member = memberService.getOpenIdMember(openid);
            // 查询结果为空
            if (member == null) {

                // 4.请求微信API，得到 扫码人微信信息，并将 扫码人微信信息 添加到数据库
                // 请求地址
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";

                // 填充 %s 占位符 (access_token, openid)
                String userInfoUrl = String.format(
                        baseUserInfoUrl,
                        access_token,
                        openid
                );

                // 使用HttpClient发送请求，请求地址为 userInfoUrl，得到返回结果 (返回结果中包含 扫码人微信信息)
                String userInfo = HttpClientUtils.get(userInfoUrl);

                // 把 userInfo 转换为 Map
                HashMap userInfoMap = gson.fromJson(userInfo, HashMap.class);
                // 获取 微信昵称
                String nickname = (String)userInfoMap.get("nickname");
                // 获取 微信头像
                String headimgurl = (String)userInfoMap.get("headimgurl");

                // 将 扫码人微信信息 添加到数据库
                member = new UcenterMember();
                member.setOpenid(openid);
                member.setNickname(nickname);
                member.setAvatar(headimgurl);
                memberService.save(member);
            }

            // 5.根据 用户信息 生成 token字符串
            String jwtToken = JwtUtils.getJwtToken(member.getId(), member.getNickname());

            // 6.重定向到 在线教育前台首页
            return "redirect:http://localhost:3000/?token=" + jwtToken;
        } catch(Exception e) {
            throw new GuliException(20001, "登录失败");
        }
    }

    @ApiOperation(value = "生成微信二维码")
    @GetMapping("/login")
    public String getWxCode() {

        // 微信开放平台授权baseUrl   (%s 相当于 占位符)
        String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
                "?appid=%s" +
                "&redirect_uri=%s" +
                "&response_type=%s" +
                "&scope=%s" +
                "&state=%s" +
                "#wechat_redirect";

        // 对 redirect_url 进行URLEncoder编码
        String redirectUrl = ConstantWxUtils.WX_OPEN_REDIRECT_URL;
        try {
            redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 填充 %s 占位符，得到 微信开放平台授权url
        String url = String.format(
                baseUrl,
                ConstantWxUtils.WX_OPEN_APP_ID,
                redirectUrl,
                "code",
                "snsapi_login",
                "atguigu"
        );

        // 重定向到 微信开放平台授权url
        return "redirect:" + url;
    }
}
