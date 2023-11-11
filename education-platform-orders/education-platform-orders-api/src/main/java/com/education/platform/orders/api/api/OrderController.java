package com.education.platform.orders.api.api;

import com.education.base.exception.EducationException;
import com.education.platform.orders.api.util.SecurityUtil;
import com.education.platform.orders.model.dto.AddOrderDto;
import com.education.platform.orders.model.dto.PayRecordDto;
import com.education.platform.orders.service.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author：kkoneone11
 * @name：OrderController
 * @Date：2023/11/9 15:41
 */

@RestController
@Slf4j
@Api(value = "订单支付接口" ,tags = "订单支付接口")
public class OrderController {

    @Autowired
    OrderService orderService;


    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EducationException.cast("请先登录");
        }
        return orderService.createOrder(user.getId(), addOrderDto);
    }

    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {

    }

}
