package com.education.auth.auth.controller;

import com.education.auth.ucenter.model.dto.FindPswDto;
import com.education.auth.ucenter.service.VerifyService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：kkoneone11
 * @name：CheckCodeController
 * @Date：2023/11/3 10:23
 */
@RestController
@Slf4j
public class CheckCodeController {

    @Autowired
    VerifyService verifyService;

    @ApiOperation(value = "找回密码",tags = "找回密码")
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPswDto findPswDto){
        verifyService.findPassword(findPswDto);
    }
}
