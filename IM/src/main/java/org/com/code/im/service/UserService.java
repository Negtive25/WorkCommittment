package org.com.code.im.service;

import org.com.code.im.pojo.User;
import org.springframework.stereotype.Service;


@Service
public interface UserService {
    User selectUserById(Long userId);
    Long selectUserIdByNameAndPasswordAndReturnUserId(User login);
    void insertUser(User user);
    Long updateUser(User user);
}
