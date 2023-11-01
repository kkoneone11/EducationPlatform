package com.education.auth.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.auth.ucenter.mapper.XcMenuMapper;
import com.education.auth.ucenter.mapper.XcUserMapper;
import com.education.auth.ucenter.model.dto.AuthParamsDto;
import com.education.auth.ucenter.model.dto.XcUserExt;
import com.education.auth.ucenter.model.po.XcMenu;
import com.education.auth.ucenter.model.po.XcUser;
import com.education.auth.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sun.security.krb5.internal.AuthContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author：kkoneone11
 * @name：UserServiceImpl
 * @Date：2023/10/16 13:04
 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcMenuMapper xcMenuMapper;

    @Autowired
    ApplicationContext applicationContext;

//    @Autowired
//    AuthService authService;


    /**
     * 根据账号查询 并组成用户信息
     * @param s
     * @return  org.springframework.security.core.userdetails.UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try{
            //将认证参数转化为AuthParamsDto
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (Exception e){
            log.info("认证请求不符合项目要求：{}",s);
            e.printStackTrace();
        }
        //认证
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        return getUserPrincipal(xcUserExt);
    }

    /**
     * @description 查询用户信息并给对应信息进行授权
     * @param xcUserExt  用户id，主键
     * @return com.education.ucenter.model.po.XcUser 用户信息用来生成JWT令牌
     * @param xcUserExt
     * @return
     */
    public UserDetails getUserPrincipal(XcUserExt xcUserExt){

        String password = xcUserExt.getPassword();

        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        ArrayList<String> permissions = new ArrayList<>();
        if(xcMenus.size() <= 0){
            //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
            permissions.add("p1");
        }else{
            xcMenus.forEach(xcMenu -> {
                permissions.add(xcMenu.getCode());
            });
        }
        xcUserExt.setPermissions(permissions);

        //为了安全在JWT令牌中不放密码
        xcUserExt.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(xcUserExt);
        String[] authorities = permissions.toArray(new String[0]);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;
    }
}
