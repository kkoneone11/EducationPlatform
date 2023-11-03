package com.education.auth.auth.controller;


import com.education.auth.ucenter.mapper.XcUserMapper;
import com.education.auth.ucenter.model.po.XcUser;
import com.education.checkcode.config.MailUtil;
import com.education.checkcode.service.SendCodeService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试controller
 * @date 2022/9/27 17:25
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    XcUserMapper userMapper;
    @Autowired
    SendCodeService sendCodeService;


    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public XcUser getuser(@PathVariable("id") String id) {
        XcUser xcUser = userMapper.selectById(id);
        return xcUser;
    }

    @RequestMapping("/r/r1")
    @PreAuthorize("hasAuthority('p1')")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    @PreAuthorize("hasAuthority('p2')")
    public String r2() {
        return "访问r2资源";
    }

    @ApiOperation(value = "发送邮箱验证码",tags = "发送邮箱验证码")
    @PostMapping("/phone")
    public void sendEMail(@RequestParam("param1") String email){
        String code = MailUtil.achieveCode();
        sendCodeService.sendEmail(email, code);
    }



}
