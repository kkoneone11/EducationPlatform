package com.education.learning.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.education.base.exception.EducationException;
import com.education.learning.service.config.PayNotifyConfig;
import com.education.learning.service.service.MyCourseTablesService;
import com.education.messagesdk.model.po.MqMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author：kkoneone11
 * @name：ReceivePayNotifyService
 * @Date：2023/11/16 23:34
 */
@Service
@Slf4j
public class ReceivePayNotifyServiceImpl {

    @Autowired
    MyCourseTablesService myCourseTablesService;


    //监听消息队列接受支付结果通知
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message, Channel channel){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //从message中获取消息体
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);
        //根据存入的消息体提取并解析
        //消息类型
        String messageType = mqMessage.getMessageType();
        //选课id
        String chooseCourseId = mqMessage.getBusinessKey1();
        //订单类型 60201表示购买课程
        String orderType = mqMessage.getBusinessKey2();
        //学习中心只负责处理支付结果的通知
        if(PayNotifyConfig.MESSAGE_TYPE.equals(messageType)){
            //学习中心只负责购买课程类订单的结果
            if("60201".equals(orderType)){
                boolean flag = myCourseTablesService.saveChooseCourseStatus(chooseCourseId);
                if(!flag){
                    EducationException.cast("保存选课记录失败");
                }
            }
        }
    }
}
