package com.education.content.api.controller;

import com.education.content.model.dto.CourseCategoryTreeDto;
import com.education.content.service.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseCategoryController
 * @Date：2023/8/8 15:29
 */
@Api(value = "课程分类接口" , tags = "课程分类接口")
@RestController
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @ApiOperation("课程分类接口")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {

        return courseCategoryService.queryTreeNodes("1");
    }

}
