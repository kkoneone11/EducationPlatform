package com.education.content.service.service;

import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.po.CoursePublish;
import com.education.search.po.CourseIndex;

import java.io.File;

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

    void commitAudit(Long companyId,Long courseId);

    void publish(Long companyId,Long courseId);

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     * @author kkoneone11
     * @date 2022/9/23 16:59
     */
    File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     * @author kkoneone11
     * @date 2022/9/23 16:59
     */
    void  uploadCourseHtml(Long courseId,File file);

    /**
     * 保存课程索引
     * @param courseId
     * @return Boolean
     */
    Boolean saveCourseIndex(Long courseId);

    CoursePublish getCoursePublish(Long courseId);
}
