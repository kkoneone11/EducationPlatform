package com.education.auth.auth.controller;

import com.education.auth.ucenter.model.po.XcUser;
import com.education.auth.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Author：kkoneone11
 * @name：WxLoginController
 * @Date：2023/10/29 15:26
 */
@Slf4j
@RestController
public class WxLoginController {

    @Autowired
    private WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(@RequestParam("code") String code,
                          @RequestParam("state") String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //向微信申请令牌，拿到令牌后查询用户信息并写入数据库
        XcUser xcUser = wxAuthService.wxAuth(code);
        //暂时硬编码
        xcUser.setUsername("t1");
        if(xcUser == null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
    }
}
