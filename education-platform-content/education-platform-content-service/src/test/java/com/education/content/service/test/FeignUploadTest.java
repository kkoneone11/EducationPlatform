package com.education.content.service.test;

import com.education.content.service.config.MultipartSupportConfig;
import com.education.content.service.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Author：kkoneone11
 * @name：FeignUploadTest
 * @Date：2023/10/3 20:02
 */
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    MediaServiceClient mediaServiceClient;
    //远程调用，上传文件
    @Test
    public void test() {

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\develop\\test.html"));
        mediaServiceClient.upload(multipartFile,"course","test.html");

    }


}


//media-api:
//        ribbon:
//        ## 服务提供者的地址，不是服务注册中心的地址
//        listOfServers: http://localhost:63050