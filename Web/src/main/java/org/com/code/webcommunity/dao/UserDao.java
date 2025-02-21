package org.com.code.webcommunity.dao;

import org.com.code.webcommunity.mapper.UserMapper;
import org.com.code.webcommunity.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class UserDao {
    @Autowired
    private UserMapper userMapper;

    public User selectUserById(int userId){
        return userMapper.selectUserById(userId);
    }

    public int selectUserIdByNameAndPassword(Map<String,Object> map){
        return userMapper.selectUserIdByNameAndPassword(map);
    }
    public int selectUserIdByName(String userName){
        return userMapper.selectUserIdByName(userName);
    }

    public int insertUser(User user){
        return userMapper.insertUser(user);
    }

    public int updateUser(Map<String,Object> map){
        return userMapper.updateUser(map);
    }
}
