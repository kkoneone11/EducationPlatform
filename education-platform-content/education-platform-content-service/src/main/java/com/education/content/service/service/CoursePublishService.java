package com.education.content.service.service;

import com.education.content.model.dto.CoursePreviewDto;

/**
 * @Author：kkoneone11
 * @name：CoursePublishService
 * @Date：2023/9/10 22:41
 */
public interface CoursePublishService {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.education.content.model.dto.CoursePreviewDto
     * @author kkoneone11
     * @date 2022/9/16 15:36
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
