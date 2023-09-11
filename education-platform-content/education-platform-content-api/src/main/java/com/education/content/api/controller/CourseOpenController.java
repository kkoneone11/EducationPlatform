package com.education.content.api.controller;

import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.service.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：kkoneone11
 * @name：CourseOpenController
 * @Date：2023/9/11 13:29
 */
@RestController
@RequestMapping("/open")
@Api(value = "课程公开查询接口", tags = "课程公开查询接口")
public class CourseOpenController {

    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId")Long courseId){
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        return coursePreviewInfo;
    }
}
