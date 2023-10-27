package com.education.auth.ucenter.service;

import com.education.auth.ucenter.model.dto.AuthParamsDto;
import com.education.auth.ucenter.model.dto.XcUserExt;

/**
 * @Author：kkoneone11
 * @name：AuthService
 * @Date：2023/10/26 22:30
 */


public interface AuthService {


    /**
     * @description 认证方法
     * @param authParamsDto 认证参数
     * @return com.education.ucenter.model.po.XcUser 用户信息
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
