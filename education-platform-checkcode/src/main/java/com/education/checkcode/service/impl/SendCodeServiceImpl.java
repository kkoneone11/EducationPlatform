package com.education.checkcode.service.impl;

import com.education.base.exception.EducationException;
import com.education.checkcode.config.MailUtil;
import com.education.checkcode.service.SendCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author：kkoneone11
 * @name：SendCodeServiceImpl
 * @Date：2023/11/3 10:35
 */
@Service
@Slf4j
public class SendCodeServiceImpl implements SendCodeService {

    public final Long CODE_TTL = 120L;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendEmail(String email, String code) {
        //向用户发送验证码
        try{
            MailUtil.sendTestMail(email,code);
        }catch (Exception e){
            log.error("邮件发送失败:{}",e.getMessage());
            EducationException.cast("发送验证码失败，请稍后再试");
        }
        //将验证码存储到redis TTL设置为2分钟
        stringRedisTemplate.opsForValue().set(email,code,CODE_TTL, TimeUnit.SECONDS);
    }
}
