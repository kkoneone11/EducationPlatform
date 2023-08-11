package com.education.content.service.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.base.exception.EducationException;
import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.Teachplan;
import com.education.content.model.po.TeachplanMedia;
import com.education.content.service.mapper.TeachplanMapper;
import com.education.content.service.mapper.TeachplanMediaMapper;
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

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;
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
     * 删除课程计划
     * @param teachplanId
     */
    @Override
    public void deleteTeachplan(Long teachplanId) {
        if(teachplanId == null){
            EducationException.cast("要删除的课程计划id为空");
        }
        //如果是删除章节的时候则要看子节是否为空
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer grade = teachplan.getGrade();

        //删除章节
        if(grade == 1){
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,teachplanId);
            //查询章节下的子节的数目
            Integer count = teachplanMapper.selectCount(queryWrapper);
            //还有子节则抛异常
            if(count > 0){
                EducationException.cast("课程计划信息还有子级信息，无法删除");
            }
            //执行
            teachplanMapper.deleteById(teachplanId);
        }else{
            //删除子节，需要删除课程信息的同时还需要删除媒体
            teachplanMapper.deleteById(teachplanId);
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);
            teachplanMediaMapper.delete(queryWrapper);
        }
    }

    /**
     * 移动课程计划顺序
     * @param moveType
     * @param teachplanId
     */
    @Override
    public void moveTeachplan(String moveType, Long teachplanId) {
        //获取当前的grade和orderby，根据章节移动和节移动分别处理
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer grade = teachplan.getGrade();
        Integer orderby = teachplan.getOrderby();
        //章节移动是在同一课程下的orderby移动
        Long courseId = teachplan.getCourseId();
        //节移动是同一章节下的orderby移动
        Long parentid = teachplan.getParentid();

        //向上移动 根据当前orderby然后排序找出其上一个的orderby然后进行交换
        if("moveup".equals(moveType)){
            //章节移动
            // SELECT * FROM teachplan WHERE courseId = 117 AND grade = 1  AND orderby < 1 ORDER BY orderby DESC LIMIT 1
            if(grade == 1){
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getCourseId,courseId)
                        .eq(Teachplan::getGrade,grade)
                        .lt(Teachplan::getOrderby,orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan temp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan,temp);
                //节移动
            }else if(grade == 2){
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid,parentid)
                        .eq(Teachplan::getGrade,grade)
                        .lt(Teachplan::getOrderby,orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan temp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan,temp);
            }
        //向下移动 根据当前orderby然后排序找出其下一个的orderby然后进行交换
        }else if("movedown".equals(moveType)){
            //章节移动
            if(grade == 1){
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);

            //节移动
            }else if(grade == 2){
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(queryWrapper);
                exchangeOrderby(teachplan, tmp);
            }
        }
    }

    /**t
     * 交换orderby
     * @param teachplan
     * @param tmp
     */
    public void exchangeOrderby(Teachplan teachplan, Teachplan tmp){
        //如果已经查不到对象了则证明已经到底了
        if(tmp == null){
            EducationException.cast("已经到底啦");
        }

        //互相交换orderby即可
        Integer orderby = teachplan.getOrderby();
        Integer tmpOrderby = tmp.getOrderby();

        teachplan.setOrderby(tmpOrderby);
        tmp.setOrderby(orderby);

        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(tmp);


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
