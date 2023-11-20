package com.education.learning.service.service;

import com.education.base.model.RestResponse;

/**
 * @Author：kkoneone11
 * @name：learningService
 * @Date：2023/11/18 16:12
 */


public interface LearningService {

    /**
     * @description 获取教学视频
     * @param courseId 课程id
     * @param teachplanId 课程计划id
     * @param mediaId 视频文件id
     * @return com.education.base.model.RestResponse<java.lang.String>
     * @author kkoneone11
     * @date 2022/10/5 9:08
     */
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
