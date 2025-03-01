package org.com.code.webcommunity.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.com.code.webcommunity.pojo.User;

import java.util.Map;

@Mapper
public interface UserMapper {

    public User selectUserById(int userId);

    public int insertUser(User user);

    public int selectUserIdByName(String userName);

    public int updateUser(Map<String,Object> map);

    //用于SpringSecurity的校验
    public User findUserByName(String userName);
}

