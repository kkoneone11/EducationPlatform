package com.education.content.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author kkoneone
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    public List<TeachplanDto> selectTreeNodes(long courseId);
}
