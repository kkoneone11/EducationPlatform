package com.education.content.service.service.jobhandler;

import com.education.base.exception.EducationException;
import com.education.content.model.po.CoursePublish;
import com.education.content.service.service.CoursePublishService;
import com.education.messagesdk.model.po.MqMessage;
import com.education.messagesdk.service.MessageProcessAbstract;
import com.education.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @Author：kkoneone11
 * @name：CoursePublishTask
 * @Date：2023/10/2 13:16
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    //课程发布任务处理
    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //存储为课程静态化
        generateCourseHtml(mqMessage,courseId);
        //存储为课程索引
        saveCourseIndex(mqMessage,courseId);
        //存储为课程缓存
        saveCourseCache(mqMessage,courseId);
        return false;
    }

    //生成课程静态化页面并上传至文件系统
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("开始课程静态化,课程id为:{}",courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        if(stageOne > 0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }

        //生成静态网页
        File file = coursePublishService.generateCourseHtml(Long.valueOf(courseId));
        if(file == null){
            EducationException.cast("课程页面静态化异常");
        }
        //保存到minio中
        coursePublishService.uploadCourseHtml(Long.valueOf(courseId),file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(id);

    }

    //保存课程索引信息
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息,课程id:{}",courseId);
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        if(stageTwo > 0){
            log.debug("当前阶段为创建课程索引任务，已完成，无需再次处理，任务信息：{}", mqMessage);
            return;
        }
        Boolean isSuccess = coursePublishService.saveCourseIndex(courseId);
        if(isSuccess){
            mqMessageService.completedStageTwo(id);
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //将课程信息缓存到redis
    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至redis，课程id：{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
