package com.education.content.service.service.Impl;

import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.service.service.CourseBaseInfoService;
import com.education.content.service.service.CoursePublishService;
import com.education.content.service.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CoursePublishServiceImpl
 * @Date：2023/9/10 22:42
 */

@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    /**
     * 封装成CoursePreviewDto
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }
}
