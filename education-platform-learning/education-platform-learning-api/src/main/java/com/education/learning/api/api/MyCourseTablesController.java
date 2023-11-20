package com.education.learning.api.api;

import com.education.base.exception.EducationException;

import com.education.base.model.page.PageResult;
import com.education.learning.model.dto.MyCourseTableParams;
import com.education.learning.model.dto.XcChooseCourseDto;
import com.education.learning.model.dto.XcCourseTablesDto;
import com.education.learning.model.po.XcCourseTables;
import com.education.learning.api.util.SecurityUtil;
import com.education.learning.service.service.MyCourseTablesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 我的课程表接口
 * @date 2022/10/25 9:40
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    MyCourseTablesService courseTablesService;
    

    @ApiOperation("添加选课记录")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EducationException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return  courseTablesService.addChooseCourse(userId, courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {

        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EducationException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        return  courseTablesService.getLearningStatus(userId, courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            EducationException.cast("请登录后继续选课");
        }
        String userId = user.getId();
//设置当前的登录用户
        params.setUserId(userId);

        return courseTablesService.mycourestabls(params);
    }

}
