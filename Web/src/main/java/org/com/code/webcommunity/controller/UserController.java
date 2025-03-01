package org.com.code.webcommunity.controller;

import org.com.code.webcommunity.exception.BadRequestException;
import org.com.code.webcommunity.pojo.User;
import org.com.code.webcommunity.service.UserService;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/api/user/seeUserProfile")
    public ResponseEntity<User> seeUserProfile() throws BadRequestException {

        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        User result = userService.selectUserById(userId);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        //不把用户的id和password返回
        result.setId(0);
        result.setPassword(null);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PostMapping("/api/user/insertUser")
    public ResponseEntity<User> insertUser(@RequestBody User user) throws BadRequestException {
        if (user == null) {
            throw new BadRequestException("用户信息不能为空");
        }
        userService.insertUser(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/api/user/updateUser")
    public ResponseEntity<User> updateUser(@RequestBody User user) throws BadRequestException {

        if (user == null) {
            throw new BadRequestException("用户信息不能为空");
        }

        int userId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

        user.setId(userId);

        int result = userService.updateUser(user);
        if (result == 0) {
            throw new BadRequestException("更新用户失败");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/user/login")
    public ResponseEntity<String> Login(@RequestBody User login) throws BadRequestException {
        if (login.getUserName() == null || login.getPassword() == null) {
            throw new BadRequestException("用户名或密码不能为空");
        }
        String token = userService.selectUserIdByNameAndPasswordAndReturnToken(login);
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping("/api/user/logout")
    public ResponseEntity<String> Logout(@RequestHeader String token) throws BadRequestException {
        JWTUtils.deleteToken(token);
        return new ResponseEntity<>("成功登出账号",HttpStatus.OK);
    }
}
