package com.education.platform.orders.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.base.exception.EducationException;
import com.education.base.utils.IdWorkerUtils;
import com.education.base.utils.QRCodeUtil;
import com.education.platform.orders.model.dto.AddOrderDto;
import com.education.platform.orders.model.dto.PayRecordDto;
import com.education.platform.orders.model.po.XcOrders;
import com.education.platform.orders.model.po.XcOrdersGoods;
import com.education.platform.orders.model.po.XcPayRecord;
import com.education.platform.orders.service.mapper.XcOrdersGoodsMapper;
import com.education.platform.orders.service.mapper.XcOrdersMapper;
import com.education.platform.orders.service.mapper.XcPayRecordMapper;
import com.education.platform.orders.service.service.OrderService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：OrderServiceImpl
 * @Date：2023/11/9 16:53
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;


    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        XcOrders orders = saveXcOrders(userId, addOrderDto);
        if(orders == null){
            EducationException.cast("订单创建失败");
        }
        if(orders.getStatus().equals("600002")){
            EducationException.cast("订单已经支付");
        }
        //生成支付记录
        XcPayRecord payRecord = createPayRecord(orders);
        //生成二维码
        String qrCode = null;
        try{
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        }catch (Exception e){
            EducationException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }



    /**
     * 保存订单消息
     * @param userId
     * @param addOrderDto
     * @return
     */

    public XcOrders saveXcOrders(String userId,AddOrderDto addOrderDto){
        //幂等性处理
        XcOrders orders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if(orders != null){
            return orders;
        }

        orders = new XcOrders();
        //生成订单
        BeanUtils.copyProperties(addOrderDto,orders);
        orders.setOutBusinessId(IdWorkerUtils.getInstance().createUUID());
        orders.setStatus("600001");
        orders.setCreateDate(LocalDateTime.now());
        orders.setUserId(userId);
        //插入订单表
        int insert = ordersMapper.insert(orders);
        if(insert <= 0){
            EducationException.cast("插入订单记录失败");
        }
        //插入订单明细表
        Long orderId = orders.getId();
        String orderDetail = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods,xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);
            ordersGoodsMapper.insert(xcOrdersGoods);
        });
        return orders;


    }

    /**
     * 根据唯一的商品订单号获取
     * @param businessId
     * @return
     */
    public XcOrders getOrderByBusinessId(String businessId){

        return ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
    }

    /**
     * 生成支付订单
     * @param orders
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders orders){
        if(orders == null){
            EducationException.cast("订单不存在");
        }
        if(orders.getStatus().equals("600002")){
            EducationException.cast("订单已经支付");
        }
        XcPayRecord payRecord = new XcPayRecord();
        payRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");  // 未支付
        payRecord.setUserId(orders.getUserId());
        int insert = payRecordMapper.insert(payRecord);
        if (insert <= 0) {
            EducationException.cast("插入支付交易记录失败");
        }
        return payRecord;
    }

    /**
     * @description 查询支付交易记录
     * @param payNo  交易记录号
     * @return com.education.orders.model.po.XcPayRecord
     * @author kkoneone11
     * @date 2022/10/20 23:38
     */
    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }

}
