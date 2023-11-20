package com.education.learning.service.service;

import com.education.base.model.page.PageResult;
import com.education.learning.model.dto.MyCourseTableParams;
import com.education.learning.model.dto.XcChooseCourseDto;
import com.education.learning.model.dto.XcCourseTablesDto;
import com.education.learning.model.po.XcCourseTables;

/**
 * @Author：kkoneone11
 * @name：MyCourseTablesService
 * @Date：2023/11/4 23:01
 */
public interface MyCourseTablesService {


    XcChooseCourseDto addChooseCourse(String userId,Long courseId);

    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    boolean saveChooseCourseStatus(String chooseCourseId);


    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);
}
