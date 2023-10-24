package com.education.auth.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.auth.ucenter.mapper.XcUserMapper;
import com.education.auth.ucenter.model.po.XcUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Author：kkoneone11
 * @name：UserServiceImpl
 * @Date：2023/10/16 13:04
 */
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;
    /**
     * 根据账号查询用户信息
     * @param s
     * @return  org.springframework.security.core.userdetails.UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, s));
        if(user == null){
            return null;
        }
        //取出数据库存储的正确密码
        String password = user.getPassword();
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities= {"p1"};
        //为了安全令牌中不放密码
        user.setPassword(null);
        //将userDetail的name转化存储为user，这样可以存储更多用户信息
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象,权限信息待实现授权功能时再向UserDetail中加入
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();

        return userDetails;
    }
}
