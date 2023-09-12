package com.education.content.service.service.Impl;

import com.alibaba.fastjson.JSON;
import com.education.base.exception.EducationException;
import com.education.content.model.dto.CourseBaseInfoDto;
import com.education.content.model.dto.CoursePreviewDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.CourseBase;
import com.education.content.model.po.CourseMarket;
import com.education.content.model.po.CoursePublish;
import com.education.content.model.po.CoursePublishPre;
import com.education.content.service.mapper.CourseBaseMapper;
import com.education.content.service.mapper.CourseMarketMapper;
import com.education.content.service.mapper.CoursePublishMapper;
import com.education.content.service.mapper.CoursePublishPreMapper;
import com.education.content.service.service.CourseBaseInfoService;
import com.education.content.service.service.CoursePublishPreService;
import com.education.content.service.service.CoursePublishService;
import com.education.content.service.service.TeachplanService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：CoursePublishServiceImpl
 * @Date：2023/9/10 22:42
 */

@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;




    /**
     * 封装成CoursePreviewDto
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree= teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }

    /**
     * 提交课程预发布信息
     * @param companyId
     * @param courseId
     */
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程基本信息，拿出条件进行约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        if("202003".equals(auditStatus)){
            //已经审核过 不允许再提交
            EducationException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构课程
        if(!courseBase.getCompanyId().equals(companyId)){
            EducationException.cast("不允许提交其它机构的课程。");
        }
        //课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            EducationException.cast("提交失败，请上传课程图片");
        }
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree.size()<=0){
            EducationException.cast("提交失败，还没有添加课程计划");
        }
        //转json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态 已提交。
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);

    }

    @Override
    public void publish(Long companyId, Long courseId) {
        //约束校验
        //先查课程预发布表看是否有记录检验是否是先审核
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            EducationException.cast("课程还未进行审核操作");
        }
        //本机构只允许提交本机构课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            EducationException.cast("不允许发布其他机构的课程");
        }
        //查看课程审核状态 看是否审核通过
        String status = coursePublishPre.getStatus();
        if("202004".equals(status)){
            EducationException.cast("课程审核通过才可发布");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }


    /**
     * 保存课程发布信息
     * @param courseId
     */
    private void saveCoursePublish(Long courseId) {
        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            EducationException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        //预发布表的数据和发布表的数据一致
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        //查看课程发布是否存在
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            //不存在则插入
            coursePublishMapper.insert(coursePublishUpdate);
        }else{
            //存在则更新
            coursePublishMapper.updateById(coursePublishUpdate);
        }
        //更新课程基本信息表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    /**
     * 保存消息表记录
     * @param courseId
     */
    private void saveCoursePublishMessage(Long courseId) {

    }
}
