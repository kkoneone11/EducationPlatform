package com.education.content.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.content.model.po.CourseTeacher;
import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author kkoneone
 */
@Mapper
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {

}
