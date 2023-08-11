package com.education.content.model.dto;

import com.education.content.model.po.Teachplan;
import com.education.content.model.po.TeachplanMedia;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：TeachplanDto
 * @Date：2023/8/10 19:21
 */

@ApiModel(value = "TeachplanDto", description = "教师计划树")
@Data
public class TeachplanDto extends Teachplan {
    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    //子结点
    List<TeachplanDto> teachPlanTreeNodes;
}
