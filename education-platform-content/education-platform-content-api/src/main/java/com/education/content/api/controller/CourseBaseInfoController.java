package com.education.content.api.controller;

import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;
import com.education.content.service.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：kkoneone11
 * @name：CourseBaseInfoController
 * @Date：2023/8/4 16:55
 */

/**
 *
 */
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

   @Autowired
   CourseBaseInfoService courseBaseInfoService;

    /**
     * pageParams分页参数通过url的key/value传入所以不用加@RequestParam，queryCourseParams通过json数据传入，使用@RequestBody注解将json转成QueryCourseParamsDto对象
     * @param pageParams
     * @param queryCourseParams
     * @return
     */
    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
        return courseBasePageResult;

    }


    @RequestMapping("/course/listTest")
    public PageResult<CourseBase> listTest(){
        return null;
    }
}
