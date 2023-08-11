package com.education.content.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.content.model.dto.SaveTeachplanDto;
import com.education.content.model.dto.TeachplanDto;
import com.education.content.model.po.Teachplan;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author：kkoneone11
 * @name：TeachplanService
 * @Date：2023/8/10 20:58
 */
public interface TeachplanService extends IService<Teachplan> {

    List<TeachplanDto> findTeachplanTree(long courseId);

    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachplan(@PathVariable Long teachplanId);

    void moveTeachplan(String moveType , Long teachplanId);
}
