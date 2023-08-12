package com.education.content.service.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.base.exception.EducationException;
import com.education.content.model.po.CourseTeacher;
import com.education.content.service.mapper.CourseTeacherMapper;
import com.education.content.service.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseTeacherServiceImpl
 * @Date：2023/8/11 21:38
 */
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId , courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        Long id = courseTeacher.getId();

        if(id == null){
            //新增课程老师
            CourseTeacher teacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher , teacher);
            teacher.setCreateDate(LocalDateTime.now());
            int i = courseTeacherMapper.insert(teacher);
            if( i <= 0 ){
                EducationException.cast("新增失败");
            }
            return teacher;
        }else{
            //修改课程老师
            CourseTeacher teacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(courseTeacher,teacher);
            int i = courseTeacherMapper.updateById(teacher);
            if( i <= 0){
                EducationException.cast("修改失败");
            }
            return teacher;
        }
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        int flag = courseTeacherMapper.delete(queryWrapper);
        if (flag < 0)
            EducationException.cast("删除失败");
    }
}
