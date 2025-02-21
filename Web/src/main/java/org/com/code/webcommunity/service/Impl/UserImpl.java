package org.com.code.webcommunity.service.Impl;

import org.com.code.webcommunity.dao.ArticleDao;
import org.com.code.webcommunity.dao.UserDao;
import org.com.code.webcommunity.exception.DatabaseException;
import org.com.code.webcommunity.pojo.User;
import org.com.code.webcommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Override
    @Transactional
    public int selectUserIdByNameAndPassword(User login) {
        try {

            Map<String, Object> map = new HashMap<>();
            map.put("userName", login.getUserName());
            map.put("password", login.getPassword());
            return userDao.selectUserIdByNameAndPassword(map);
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
    public int insertUser(User user) {
        try {
            return userDao.insertUser(user);
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
