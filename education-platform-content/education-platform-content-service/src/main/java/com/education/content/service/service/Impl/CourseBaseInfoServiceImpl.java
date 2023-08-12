package com.education.content.service.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.base.exception.EducationException;
import com.education.base.model.page.PageParams;
import com.education.base.model.page.PageResult;
import com.education.content.model.dto.AddCourseDto;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.EditCourseDto;
import com.education.content.model.dto.QueryCourseParamsDto;
import com.education.content.model.po.*;
import com.education.content.service.mapper.*;
import com.education.content.service.service.CourseBaseInfoService;
import com.education.content.service.service.CourseCategoryService;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CourseBaseInfoServiceImpl
 * @Date：2023/8/6 11:14
 */
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper ,CourseBase> implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

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

    /**
     * 创建课程 因为新增课程操作两个表所以需要用到@Transactional
     * @param companyId
     * @param dto
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new EducationException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new EducationException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new EducationException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new EducationException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new EducationException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new EducationException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new EducationException("收费规则为空");
        }
        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto,courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
            throw new EducationException("新增课程基本信息失败");
        }
        //向课程营销表保存课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto,courseMarketNew);
        courseMarketNew.setId(courseId);
        int res = saveCourseMarket(courseMarketNew);
        if(res<=0){
            throw new EducationException("保存课程营销信息失败");
        }

        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);

    }

    /**
     * 保存课程营销表
     * @param courseMarketNew
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //先查看charge是否为空，为空则返回不能为空
        if(StringUtils.isBlank(courseMarketNew.getCharge())){
            throw new RuntimeException("收费规则没有选择");
        }
        //课程为收费,价格不能为空且必须大于0
        if(courseMarketNew.getCharge().equals("201001")){
            if(courseMarketNew.getPrice() ==null
                    ||courseMarketNew.getPrice().floatValue() <0){
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
            //1.对象不存在则直接插入
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            //2.对象存在则执行更新
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    /**
     * 查询课程信息
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }


    /**
     * 更新课程 因为更新课程操作两个表所以需要用到@Transactional
     * @param companyId
     * @param dto
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto dto) {
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            EducationException.cast("课程不存在");
        }
        //本机构只能修改本机构的课程
        if(!companyId.equals(courseBase.getCompanyId())){
            EducationException.cast("本机构只能修改本机构的课程");
        }
        //封装课程基本信息表 进行更新
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);
        if(i <= 0){
            EducationException.cast("更新失败");
        }

        //封装课程营销信息表 进行更新
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        saveCourseMarket(courseMarket);

        //查询并返回课程信息
        return getCourseBaseInfo(courseId);
    }

    @Transactional
    @Override
    public void delectCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId()))
            EducationException.cast("只允许删除本机构的课程");
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        // 删除营销信息
        courseMarketMapper.deleteById(courseId);
        // 删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }


}
