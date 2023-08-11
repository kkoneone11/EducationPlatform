package com.education.content.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.EditCourseDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author：kkoneone11
 * @name：CourseBaseInfoService
 * @Date：2023/8/6 11:09
 */
public interface CourseBaseInfoService extends IService<CourseBase> {

    /**
     * 课程查询接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto);

    CourseBaseInfoDto getCourseBaseInfo(Long courseId);
}
