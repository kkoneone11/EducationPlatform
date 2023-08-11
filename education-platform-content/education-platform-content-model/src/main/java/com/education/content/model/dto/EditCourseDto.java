package com.education.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author：kkoneone11
 * @name：EditCourseDto
 * @Date：2023/8/10 11:56
 */
@ApiModel(value = "EditCourseDto", description="修改课程基本信息")
@Data
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程id", required = true)
    private Long id;

}
