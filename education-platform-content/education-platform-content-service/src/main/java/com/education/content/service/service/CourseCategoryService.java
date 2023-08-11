package com.education.content.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.content.model.dto.CourseCategoryTreeDto;
import com.education.content.model.po.CourseCategory;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseCategoryInfoService
 * @Date：2023/8/9 10:21
 */
public interface CourseCategoryService extends IService<CourseCategory>{
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
