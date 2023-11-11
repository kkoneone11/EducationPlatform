package com.education.platform.orders.service.service;

import com.education.platform.orders.model.dto.AddOrderDto;
import com.education.platform.orders.model.dto.PayRecordDto;

/**
 * @Author：kkoneone11
 * @name：OrderService
 * @Date：2023/11/9 16:52
 */
public interface OrderService {
    /**
     * @description 创建商品订单
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @author Mr.M
     * @date 2022/10/4 11:02
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);
}
