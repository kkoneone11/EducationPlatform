package com.education.auth.ucenter.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.auth.ucenter.feignclient.CheckCodeClient;
import com.education.auth.ucenter.mapper.XcUserMapper;
import com.education.auth.ucenter.model.dto.FindPswDto;
import com.education.auth.ucenter.model.po.XcUser;
import com.education.auth.ucenter.service.VerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Author：kkoneone11
 * @name：VerifyServiceImpl
 * @Date：2023/11/3 12:26
 */
@Service
public class VerifyServiceImpl implements VerifyService {

    @Autowired
    XcUserMapper userMapper;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public void findPassword(FindPswDto findPswDto) {
        String email = findPswDto.getEmail();
        String checkcode = findPswDto.getCheckcode();
        Boolean verify = checkCodeClient.verify(email, checkcode);
        if (!verify) {
            throw new RuntimeException("验证码输入错误");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        LambdaQueryWrapper<XcUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(XcUser::getEmail, findPswDto.getEmail());
        XcUser user = userMapper.selectOne(lambdaQueryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userMapper.updateById(user);
    }
}
