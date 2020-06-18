package com.atguigu.eduorder.service;

import com.atguigu.eduorder.entity.PayLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 支付日志表 服务类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-17
 */
public interface PayLogService extends IService<PayLog> {

    // 生成微信支付二维码
    Map generateQRCode(String orderNo);

    // 根据订单号查询订单状态
    Map<String, String> queryPayStatus(String orderNo);

    // 更新订单状态 & 添加支付记录
    void updateOrdersStatus(Map<String, String> map);
}
