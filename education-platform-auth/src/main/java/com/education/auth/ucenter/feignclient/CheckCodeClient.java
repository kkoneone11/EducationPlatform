package com.education.auth.ucenter.feignclient;

import com.education.auth.auth.config.CheckCodeClientFacroty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author：kkoneone11
 * @name：CheckCodeClient
 * @Date：2023/10/27 23:53
 */
@FeignClient(value = "checkcode",fallbackFactory = CheckCodeClientFacroty.class)
@RequestMapping("/checkcode")
public interface CheckCodeClient {

    @PostMapping("/verify")
    public Boolean verify(@RequestParam("key") String key,@RequestParam("code") String code);
}
