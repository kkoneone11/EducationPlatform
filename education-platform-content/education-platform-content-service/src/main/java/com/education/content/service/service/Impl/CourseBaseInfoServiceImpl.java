package com.education.content.service.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.CourseBase;
import com.education.content.service.mapper.CourseBaseMapper;
import com.education.content.service.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseBaseInfoServiceImpl
 * @Date：2023/8/6 11:14
 */
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper ,CourseBase> implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //创建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //创建查询条件，根据课程名模糊查询
        queryWrapper.like(StringUtils.isNotBlank(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getAuditStatus());

        //创建查询条件，根据课程状态查询
        queryWrapper.eq(StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());

        //构造分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //执行sql语句
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //构造PageResult<CourseBase>
        List<CourseBase> pageResultRecords = pageResult.getRecords();
        long total = pageResult.getTotal();
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(pageResultRecords,total,
                pageParams.getPageNo(),
                pageParams.getPageSize());

        return courseBasePageResult;
    }
}
