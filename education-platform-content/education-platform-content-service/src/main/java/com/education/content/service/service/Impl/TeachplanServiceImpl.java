package com.education.content.service.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.base.exception.EducationException;
import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.Teachplan;
import com.education.content.service.mapper.TeachplanMapper;
import com.education.content.service.service.TeachplanService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：TeachplanServiceImpl
 * @Date：2023/8/10 21:05
 */
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    /**
     * 查看课程计划
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    /**
     * 保存或者更新课程计划
     * @param saveTeachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //先判断是否存在id
        Long id = saveTeachplanDto.getId();

        if(id != null){
            //存在则是修改课程计划
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //不存在则是新增课程计划
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            //取出同父同级别的课程计划数量并进行设置最新的序列号
            int teachplanCount = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());

            teachplan.setOrderby(teachplanCount+1);
            int insert = teachplanMapper.insert(teachplan);
            if(insert <= 0){
                EducationException.cast("新增课程计划失败");
            }
        }

    }


    /**
     * 获取最新课程排序序号
     * @param courseId
     * @param parentId
     * @return
     */
    public int getTeachplanCount(Long courseId , Long parentId){
        //构造一个lamda构造器
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        //设置查询条件
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
