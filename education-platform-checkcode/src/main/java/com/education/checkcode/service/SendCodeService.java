package com.education.checkcode.service;

/**
 * @Author：kkoneone11
 * @name：SendCodeService
 * @Date：2023/11/3 10:21
 */
public interface SendCodeService {

    /**
     * 向邮箱发送验证码
     * @param email
     * @param code
     */
    void sendEmail(String email,String code);
}
