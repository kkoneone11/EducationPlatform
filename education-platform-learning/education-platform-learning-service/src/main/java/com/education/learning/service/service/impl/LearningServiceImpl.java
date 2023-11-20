package com.education.learning.service.service.impl;

import com.education.base.exception.EducationException;
import com.education.base.model.RestResponse;
import com.education.content.model.po.CoursePublish;
import com.education.content.model.po.Teachplan;
import com.education.learning.model.dto.XcCourseTablesDto;
import com.education.learning.service.feignclient.ContentServiceClient;
import com.education.learning.service.feignclient.MediaServiceClient;
import com.education.learning.service.service.LearningService;
import com.education.learning.service.service.MyCourseTablesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author：kkoneone11
 * @name：LearningServiceImpl
 * @Date：2023/11/18 16:13
 */

@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //1.先查询课程信息是否存在
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            EducationException.cast("课程信息不存在");
        }
        //2.判断试学规则 远程调用内容管理服务，查询教学计划teachplan
        Teachplan teachplan = contentServiceClient.getTeachplan(teachplanId);
        //2.1 isPreview字段为1则为试学 返回课程url
        if("1".equals(teachplan.getIsPreview())){
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        //3.非试学 用户登录
        if(StringUtils.isNotEmpty(userId)){
            //3.1判断是否选课 根据选课情况判断学习资格
            //学习资格状态 [{"code":"702001","desc":"正常学习"},
            // {"code":"702002","desc":"没有选课或选课后没有支付"},
            // {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if("702002".equals(learnStatus)){
                RestResponse.validfail("没有选课或者选课后没有支付");
            }else if("702003".equals(learnStatus)){
                RestResponse.validfail("已过期需要申请续期或重新支付");
            }else{
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }
        //4.非试学 用户没有登录
        String charge = coursepublish.getCharge();
        //4.1免费课程 charge为201000
        if("201000".equals(charge)){
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validfail("请购买课程后学习");
    }
}
