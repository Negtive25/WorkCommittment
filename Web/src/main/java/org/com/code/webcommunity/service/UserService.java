package org.com.code.webcommunity.service;

import org.com.code.webcommunity.pojo.User;
import org.springframework.stereotype.Service;


@Service
public interface UserService {
    User selectUserById(int userId);
    String selectUserIdByNameAndPasswordAndReturnToken(User login);
    void insertUser(User user);
    int updateUser(User user);
}
