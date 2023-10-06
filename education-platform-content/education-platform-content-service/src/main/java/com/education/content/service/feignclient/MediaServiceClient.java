package com.education.content.service.feignclient;

import com.education.content.service.config.MediaServiceClientFallbackFactory;
import com.education.content.service.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author：kkoneone11
 * @name：MediaServiceClient
 * @Date：2023/10/3 14:39
 */
@Service
@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class,fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile upload,
                      @RequestParam(value = "folder", required = false) String folder,
                      @RequestParam(value = "objectName",required = false) String objectName);


}
