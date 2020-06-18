package com.atguigu.eduorder.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.eduorder.constant.WeChatPayConstant;
import com.atguigu.eduorder.entity.Order;
import com.atguigu.eduorder.entity.PayLog;
import com.atguigu.eduorder.mapper.PayLogMapper;
import com.atguigu.eduorder.service.OrderService;
import com.atguigu.eduorder.service.PayLogService;
import com.atguigu.eduorder.utils.HttpClient;
import com.atguigu.servicebase.exceptionhandler.GuliException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 支付日志表 服务实现类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-17
 */
@Service
public class PayLogServiceImpl extends ServiceImpl<PayLogMapper, PayLog> implements PayLogService {

    @Autowired
    private OrderService orderService;

    // 生成微信支付二维码
    @Override
    public Map generateQRCode(String orderNo) {
        try {
            // 1.根据订单号查询订单信息
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("order_no", orderNo);
            Order order = orderService.getOne(wrapper);

            // 2.设置生成二维码需要的参数
            Map map = new HashMap();
            // 公众账号ID
            map.put("appid", WeChatPayConstant.APP_ID);
            // 商户号
            map.put("mch_id", WeChatPayConstant.MCH_ID);
            // 随机字符串
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            // 商品描述
            map.put("body", order.getCourseTitle());
            // 商户订单号
            map.put("out_trade_no", orderNo);
            // 订单金额
            map.put("total_fee", order.getTotalFee().multiply(new BigDecimal("100")).longValue() + "");
            // 终端IP
            map.put("spbill_create_ip", "127.0.0.1");
            // 回调地址
            map.put("notify_url", WeChatPayConstant.NOTIFY_URL);
            // 交易类型
            map.put("trade_type", "NATIVE");

            // 3.向微信支付提供的固定地址发送请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            // 设置xml格式的参数
            client.setXmlParam(WXPayUtil.generateSignedXml(map, WeChatPayConstant.MCH_KEY));
            client.setHttps(true);
            // 发送POST请求
            client.post();

            // 4.得到请求结果
            String xml = client.getContent();
            // 把 xml格式 转换为 map集合
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);

            // 5.封装最终返回结果
            map = new HashMap();
            // 商户订单号
            map.put("out_trade_no", orderNo);
            // 课程id
            map.put("course_id", order.getCourseId());
            // 订单金额
            map.put("total_fee", order.getTotalFee());
            // 业务结果
            map.put("result_code", resultMap.get("result_code"));
            // 二维码地址
            map.put("code_url", resultMap.get("code_url"));

            return map;
        } catch (Exception e) {
            throw new GuliException(20001, "生成二维码失败");
        }
    }

    // 根据订单号查询订单状态
    @Override
    public Map<String, String> queryPayStatus(String orderNo) {
        try {
            // 1.封装参数
            Map map = new HashMap<>();
            // 公众账号ID
            map.put("appid", WeChatPayConstant.APP_ID);
            // 商户号
            map.put("mch_id", WeChatPayConstant.MCH_ID);
            // 商户订单号
            map.put("out_trade_no", orderNo);
            // 随机字符串
            map.put("nonce_str", WXPayUtil.generateNonceStr());

            // 2.向微信支付提供的固定地址发送请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            // 设置xml格式的参数
            client.setXmlParam(WXPayUtil.generateSignedXml(map, WeChatPayConstant.MCH_KEY));
            client.setHttps(true);
            // 发送POST请求
            client.post();

            // 3.得到请求结果
            String xml = client.getContent();
            // 把 xml格式 转换为 map集合
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);

            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    // 更新订单状态 & 添加支付记录
    @Override
    public void updateOrdersStatus(Map<String, String> map) {
        // 商户订单号
        String orderNo = map.get("out_trade_no");
        // 根据订单号查询订单信息
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = orderService.getOne(wrapper);

        // 更新订单状态
        if (order.getStatus().intValue() == 1) {
            return;
        }
        order.setStatus(1); //订单状态（0：未支付 1：已支付）
        orderService.updateById(order);

        // 添加支付记录
        PayLog payLog = new PayLog();
        // 订单号
        payLog.setOrderNo(orderNo);
        // 订单完成时间
        payLog.setPayTime(new Date());
        // 支付类型（1：微信 2：支付宝）
        payLog.setPayType(1);
        // 订单金额
        payLog.setTotalFee(order.getTotalFee());
        // 支付状态
        payLog.setTradeState(map.get("trade_state"));
        // 订单流水号
        payLog.setTransactionId(map.get("transaction_id"));
        // 其他属性
        payLog.setAttr(JSONObject.toJSONString(map));
        baseMapper.insert(payLog);
    }
}
