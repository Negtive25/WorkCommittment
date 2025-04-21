package org.com.code.im.service.Impl;

import jakarta.annotation.Resource;
import org.com.code.im.config.DBUserDetailsManager;
import org.com.code.im.exception.DatabaseException;
import org.com.code.im.mapper.UserMapper;
import org.com.code.im.pojo.User;
import org.com.code.im.service.UserService;
import org.com.code.im.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Resource
    private DBUserDetailsManager dbUserDetailsManager;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public Long selectUserIdByNameAndPasswordAndReturnUserId(User login) {
        try {

            //验证登录信息,先把登录时候输入的账号密码包装成一个AuthenticationToken对象
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getUserName(), login.getPassword());
            //然后调用 AuthenticationManager 的 authenticate 调用 DBUserDetailsManager.loadUserByUsername 方法从数据库加载用户信息,进行对比认证
            //如果认证成功，返回包含用户信息和权限的 authentication 对象。
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            //如果认证失败，返回 null
            if(Objects.isNull(authentication)){
                throw new DatabaseException("账号或密码错误");
            }

            //查询数据库获取用户id
            return userMapper.selectUserIdByName(login.getUserName());
        }catch (Exception e) {
            throw new DatabaseException("账号或密码错误");
        }
    }

    @Override
    @Transactional
    public User selectUserById(Long userId) {
        try {
            return userMapper.selectUserById(userId);
        }catch (Exception e) {
            throw new DatabaseException("数据库查询用户发生错误");
        }
    }


    @Override
    @Transactional
    public void insertUser(User user) {
        try {
            User userDetails = new User(user.getUserName(), passwordEncoder.encode(user.getPassword()), user.getEmail(), "ROLE_USER");
            dbUserDetailsManager.myCreateUser(userDetails);
        }catch (Exception e) {
            throw new DatabaseException("数据库插入用户发生错误,可能是用户名重复");
        }
    }

    @Override
    @Transactional
    public Long updateUser(User user) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("userName", user.getUserName());
            if (user.getPassword() != null)
                map.put("password", passwordEncoder.encode(user.getPassword()));
            map.put("email", user.getEmail());
            map.put("avatar", user.getAvatar());
            map.put("bio", user.getBio());

            return userMapper.updateUser(map);
        }catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("数据库修改用户发生错误");
        }
    }
}
