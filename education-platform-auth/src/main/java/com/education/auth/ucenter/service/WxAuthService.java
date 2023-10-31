package com.education.auth.ucenter.service;

import com.education.auth.ucenter.model.po.XcUser;

/**
 * @Author：kkoneone11
 * @name：WxAuthService
 * @Date：2023/10/30 18:31
 */
public interface WxAuthService {
    public XcUser wxAuth(String code);
}
