package com.education.content.api.controller;

import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.service.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author：kkoneone11
 * @name：CoursePublishController
 * @Date：2023/9/10 20:39
 */
@Api(value = "课程发布接口",tags = "课程发布接口")
@RestController
public class CoursePublishController {
    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 课程预览
     * @param courseId
     * @return
     */
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /**
     * 提交审核
     * @param courseId
     */
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);
    }

    /**
     * 课程发布
     * @param courseId
     */
    @ApiOperation(value = "课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void Coursepublish(@PathVariable("courseId") Long courseId){

    }

}
