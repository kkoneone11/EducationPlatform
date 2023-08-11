package com.education.content.model.dto;

import com.education.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseCategoryTreeDto
 * @Date：2023/8/8 15:19
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
