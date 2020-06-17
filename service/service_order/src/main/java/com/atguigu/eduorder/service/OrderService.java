package com.atguigu.eduorder.service;

import com.atguigu.eduorder.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author zli78122
 * @since 2020-06-17
 */
public interface OrderService extends IService<Order> {

    // 生成订单
    String createOrders(String courseId, String memberId);
}
