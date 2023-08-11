package com.education.content.service.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.content.model.dto.CourseCategoryTreeDto;
import com.education.content.model.po.CourseCategory;
import com.education.content.service.mapper.CourseCategoryMapper;
import com.education.content.service.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author：kkoneone11
 * @name：CourseCategoryInfoServiceImpl
 * @Date：2023/8/9 10:21
 */
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //用mapper调用方法查询一下结果
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //通过Stream流List转化成map 方便使用 排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        //再组装回List
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //再遍历每个courseCategoryTreeDtos 排除根节点
        courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .forEach(item ->{
                    //找到id结点的子节点重新组装
                    if(item.getParentid().equals(id)){
                        categoryTreeDtos.add(item);
                    }
                    //找到当前节点的父节点
                    CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
                    if(courseCategoryTreeDto!=null){
                        if(courseCategoryTreeDto.getChildrenTreeNodes() == null){
                            courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        //往ChildrenTreeNodes属性中放子节点
                        courseCategoryTreeDto.getChildrenTreeNodes().add(item);
                    }
                });
        return categoryTreeDtos;
    }
}
