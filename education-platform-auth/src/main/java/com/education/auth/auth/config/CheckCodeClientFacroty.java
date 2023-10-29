package com.education.auth.auth.config;

import com.education.auth.ucenter.feignclient.CheckCodeClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author：kkoneone11
 * @name：CheckCodeClientFacroty
 * @Date：2023/10/27 23:55
 */
@Component
@Slf4j
public class CheckCodeClientFacroty implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("调用验证码服务熔断异常:{}",throwable.getMessage());
                return null;
            }
        };
    }
}
