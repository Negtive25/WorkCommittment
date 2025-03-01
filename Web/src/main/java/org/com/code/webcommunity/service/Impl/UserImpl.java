package org.com.code.webcommunity.service.Impl;

import jakarta.annotation.Resource;
import org.com.code.webcommunity.config.DBUserDetailsManager;
import org.com.code.webcommunity.dao.ArticleDao;
import org.com.code.webcommunity.dao.UserDao;
import org.com.code.webcommunity.exception.DatabaseException;
import org.com.code.webcommunity.pojo.User;
import org.com.code.webcommunity.service.UserService;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private ArticleDao articleDao;
    @Resource
    private DBUserDetailsManager dbUserDetailsManager;

    @Override
    @Transactional
    public String selectUserIdByNameAndPasswordAndReturnToken(User login) {
        try {
            //SpringSecurity验证用户名密码
            dbUserDetailsManager.loadUserByUsername(login.getUserName());
            //查询数据库获取用户id
            int userId = userDao.selectUserIdByNameAndPassword(login.getUserName());
            //生成token并返回
            return JWTUtils.getJwtToken(userId);
        }catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("账号或密码错误");
        }
    }

    @Override
    @Transactional
    public User selectUserById(int userId) {
        try {
            return userDao.selectUserById(userId);
        }catch (Exception e) {
            throw new DatabaseException("数据库查询用户发生错误");
        }
    }


    @Override
    @Transactional
    public void insertUser(User user) {
        try {
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withDefaultPasswordEncoder()
                    .username(user.getUserName()) //自定义用户名
                    .password(user.getPassword()) //自定义密码
                    .build();
            dbUserDetailsManager.createUser(userDetails);

        }catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException("数据库插入用户发生错误,可能是用户名重复");
        }
    }

    @Override
    @Transactional
    public int updateUser(User user) {
        try {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> updateAuthorNameOfArticles = new HashMap<>();
            map.put("id", user.getId());
            map.put("userName", user.getUserName());
            map.put("password", user.getPassword());
            map.put("avatar", user.getAvatar());
            map.put("bio", user.getBio());

            updateAuthorNameOfArticles.put("authorName", user.getUserName());
            updateAuthorNameOfArticles.put("authorId", user.getId());
            articleDao.updateAuthorNameOfArticles(updateAuthorNameOfArticles);

            return userDao.updateUser(map);
        }catch (Exception e) {
            throw new DatabaseException("数据库修改用户发生错误");
        }
    }
}
