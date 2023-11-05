package com.education.learning.service.feignclient;

import com.education.content.model.po.CoursePublish;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author：kkoneone11
 * @name：ContentServiceClientFallbackFactory
 * @Date：2023/11/4 21:52
 */
@Component
@Slf4j
public class ContentServiceClientFallbackFactory implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public CoursePublish getCoursepublish(Long courseId) {
                log.error("调用内容管理服务发生熔断：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
