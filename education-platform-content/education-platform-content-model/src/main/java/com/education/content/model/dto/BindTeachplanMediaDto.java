package com.education.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author：kkoneone11
 * @name：BindTeachplanMediaDto
 * @Date：2023/9/9 16:01
 */
@Data
@ApiModel(value = "BindTeachplanMediaDto",description = "课程计划绑定媒资文件")
public class BindTeachplanMediaDto {
    @ApiModelProperty(value = "媒资文件id",required = true)
    private String mediaId;
    @ApiModelProperty(value = "媒资文件名",required = true)
    private String fileName;
    @ApiModelProperty(value = "课程计划标识id",required = true)
    private Long teachplanId;
}
