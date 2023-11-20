package com.education.learning.service.feignclient;

import com.education.content.model.po.CoursePublish;
import com.education.content.model.po.Teachplan;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author：kkoneone11
 * @name：ContentServiceClient
 * @Date：2023/11/4 21:51
 */
@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

    @RequestMapping("/content/r/coursepublish/{courseId}")
    CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);

    /**
     * 获取教学计划
     * @param teachplanId
     * @return
     */
    @PostMapping("/content/teachplan/{teachplanId}")
    Teachplan getTeachplan(@PathVariable Long teachplanId);
}
