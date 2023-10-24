package com.education.content.api.controller;

import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.content.api.util.SecurityUtil;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.EditCourseDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;
import com.education.content.service.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
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

    @ApiOperation("新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }


    @ApiOperation("根据课程id查询课程接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){

        //取出当前用户身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("根据id修改课程接口")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        Long companyId = 1232141425L;
        courseBaseInfoService.delectCourse(companyId,courseId);
    }

}
