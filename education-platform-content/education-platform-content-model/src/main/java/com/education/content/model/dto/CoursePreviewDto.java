package com.education.content.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CoursePreviewDto
 * @Date：2023/9/10 22:32
 */

@Data
@ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //TODO 师资信息
}
