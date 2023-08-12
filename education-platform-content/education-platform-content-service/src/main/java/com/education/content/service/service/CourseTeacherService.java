package com.education.content.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseTeacherService
 * @Date：2023/8/11 21:34
 */

public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> getCourseTeacherList(Long courseId);

    CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher);

    void deleteCourseTeacher(Long courseId, Long teacherId);
}
