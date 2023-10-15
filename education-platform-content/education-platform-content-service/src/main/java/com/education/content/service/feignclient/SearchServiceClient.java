package com.education.content.service.feignclient;

import com.education.search.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author：kkoneone11
 * @name：SearchServiceClient
 * @Date：2023/10/10 19:14
 */
@FeignClient(value = "search")
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
