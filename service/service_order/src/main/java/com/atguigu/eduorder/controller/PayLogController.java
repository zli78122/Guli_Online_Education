package com.atguigu.eduorder.controller;

import com.atguigu.commonutils.R;
import com.atguigu.eduorder.service.PayLogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 支付日志表 前端控制器
 * </p>
 *
 * @author zli78122
 * @since 2020-06-17
 */
@RestController
@RequestMapping("/eduorder/paylog")
@CrossOrigin
public class PayLogController {

    @Autowired
    private PayLogService payLogService;

    @ApiOperation(value = "生成微信支付二维码")
    @GetMapping("/generateQRCode/{orderNo}")
    public R generateQRCode(@PathVariable String orderNo) {
        // map集合中封装了 二维码地址 和 其他必要的订单信息
        Map map = payLogService.generateQRCode(orderNo);
        return R.ok().data(map);
    }

    @ApiOperation(value = "根据订单号查询订单状态")
    @GetMapping("/queryPayStatus/{orderNo}")
    public R queryPayStatus(@PathVariable String orderNo) {
        // 根据订单号查询订单状态
        Map<String,String> map = payLogService.queryPayStatus(orderNo);
        if(map == null) {
            return R.error().message("支付出错了");
        }
        // 如果map不为空，则通过map获取支付状态
        if(map.get("trade_state").equals("SUCCESS")) { //支付成功
            // 更新订单状态 & 添加支付记录
            payLogService.updateOrdersStatus(map);
            return R.ok().message("支付成功");
        }
        return R.ok().code(25000).message("支付进行中...");
    }
}
