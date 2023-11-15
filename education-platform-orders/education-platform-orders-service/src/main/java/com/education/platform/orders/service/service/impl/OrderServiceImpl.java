package com.education.platform.orders.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.base.exception.EducationException;
import com.education.base.utils.IdWorkerUtils;
import com.education.base.utils.QRCodeUtil;
import com.education.platform.orders.model.dto.AddOrderDto;
import com.education.platform.orders.model.dto.PayRecordDto;
import com.education.platform.orders.model.dto.PayStatusDto;
import com.education.platform.orders.model.po.XcOrders;
import com.education.platform.orders.model.po.XcOrdersGoods;
import com.education.platform.orders.model.po.XcPayRecord;
import com.education.platform.orders.service.config.AlipayConfig;
import com.education.platform.orders.service.mapper.XcOrdersGoodsMapper;
import com.education.platform.orders.service.mapper.XcOrdersMapper;
import com.education.platform.orders.service.mapper.XcPayRecordMapper;
import com.education.platform.orders.service.service.OrderService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Author：kkoneone11
 * @name：OrderServiceImpl
 * @Date：2023/11/9 16:53
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Autowired
    OrderServiceImpl currentProxy;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


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

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    @Override
    public PayRecordDto queryPayResult(String payNo) {
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            EducationException.cast("请重新点击支付获取二维码");
        }
        //支付状态
        String status = payRecord.getStatus();
        //如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }
        //从支付宝查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //保存支付结果
        currentProxy.saveAliPayStatus(payStatusDto);
        //重新查询支付记录
        payRecord = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;

    }

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo){
        //========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                EducationException.cast("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            EducationException.cast("请求支付查询查询失败");
        }

        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;

    }

    /**
     * @description 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     * @return void
     * @author kkoneone11
     * @date 2022/10/4 16:52
     */
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto){
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if(payRecord == null){
            EducationException.cast("支付记录不存在");
        }
        log.debug("支付结果为：{}，支付记录：{}",payStatusDto.toString(),payRecord.toString());
        String trade_status = payStatusDto.getTrade_status();
        if(trade_status.equals("TRADE_SUCCESS")){
            //支付金额变为分
            Float totalPrice = payRecord.getTotalPrice() * 100;
            Float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) * 100;
            if(!payStatusDto.equals(APP_ID) || totalPrice.intValue() != total_amount.intValue()){
                //校验失败
                log.info("根据支付记录[{}}]找不到订单", payRecord.toString());
                EducationException.cast("根据支付记录找不到订单");
            }
            log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, trade_status);
            XcPayRecord payRecord_u = new XcPayRecord();
            payRecord_u.setStatus("601002");//支付成功
            payRecord_u.setOutPayChannel("Alipay");
            payRecord_u.setOutPayNo(payStatusDto.getTrade_no());//支付宝交易号
            payRecord_u.setPaySuccessTime(LocalDateTime.now());//通知时间
            int update1 = payRecordMapper.update(payRecord_u, new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
            if (update1 > 0) {
                log.info("更新支付记录状态成功:{}", payRecord_u.toString());
            } else {
                log.info("更新支付记录状态失败:{}", payRecord_u.toString());
                EducationException.cast("更新支付记录状态失败");
            }
            //关联的订单号
            Long orderId = payRecord.getOrderId();
            XcOrders orders = ordersMapper.selectById(orderId);
            if (orders == null) {
                log.info("根据支付记录[{}}]找不到订单", payRecord_u.toString());
                EducationException.cast("根据支付记录找不到订单");
            }
            XcOrders order_u = new XcOrders();
            order_u.setStatus("600002");//支付成功
            int update = ordersMapper.update(order_u, new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
            if (update > 0) {
                log.info("更新订单表状态成功,订单号:{}", orderId);
            } else {
                log.info("更新订单表状态失败,订单号:{}", orderId);
                EducationException.cast("更新订单表状态失败");
            }

        }

    }

}
