package com.education.platform.orders.service.service;

import com.education.platform.orders.model.dto.AddOrderDto;
import com.education.platform.orders.model.dto.PayRecordDto;
import com.education.platform.orders.model.dto.PayStatusDto;
import com.education.platform.orders.model.po.XcPayRecord;

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
     * @author kkoneone
     * @date 2022/10/4 11:02
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * @description 查询支付交易记录
     * @param payNo  交易记录号
     * @return com.education.orders.model.po.XcPayRecord
     * @author kkoneone11
     * @date 2022/10/20 23:38
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(String payNo);


    /**
     * 保存支付查询的状态
     * @param payStatusDto
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto);
}
