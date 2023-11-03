package com.education.auth.ucenter.service;

import com.education.auth.ucenter.model.dto.FindPswDto;
import com.education.auth.ucenter.model.dto.RegisterDto;

/**
 * @Author：kkoneone11
 * @name：VerifyService
 * @Date：2023/11/3 10:34
 */
public interface VerifyService {

    void findPassword(FindPswDto findPswDto);

    void register(RegisterDto registerDto);
}