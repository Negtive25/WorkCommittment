package org.com.code.im.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.com.code.im.exception.BadRequestException;
import org.com.code.im.pojo.User;
import org.com.code.im.responseHandler.ResponseHandler;
import org.com.code.im.service.UserService;
import org.com.code.im.utils.GeoIpUtil;
import org.com.code.im.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 这里应用层的接口是同步返回的,web的普通http请求直接获取返回结果
 */

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Qualifier("redisTemplateLong")
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("strRedisTemplate")
    RedisTemplate strRedisTemplate;



    //这里没有用到消息队列
    @GetMapping("/api/user/seeUserProfile")
    public ResponseHandler seeUserProfile(@RequestParam(value = "userId", required = false) String userId) throws BadRequestException {

        long id=0;
        if (userId == null|| userId.isEmpty())
            id = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        else{
            id = Long.parseLong(userId);
            if(!redisTemplate.opsForSet().isMember("AllUserId",id))
                return new ResponseHandler(ResponseHandler.BAD_REQUEST,"该用户不存在");
        }

        User result = userService.selectUserById(id);
        Map map=result.toMap();
        map.put("IPLocation",strRedisTemplate.opsForHash().get("UserIPLocation",String.valueOf(id)));
        return new ResponseHandler(ResponseHandler.SUCCESS,"查看用户主页信息",map);
    }

    //更新用户信息属于强一致性操作，直接写入 MySQL 数据库即可，无需使用消息队列。
    @PutMapping("/api/user/updateUser")
    public ResponseHandler updateUser(@RequestBody User user) throws BadRequestException {

        if (user == null) {
            throw new BadRequestException("用户信息不能为空");
        }

        long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        user.setId(userId);

        long result = userService.updateUser(user);
        if (result == 0)
            return new ResponseHandler(ResponseHandler.ERROR,"更新用户","更新失败");
        return new ResponseHandler(ResponseHandler.SUCCESS,"更新用户",result);
    }

    //用户注册属于强一致性操作，需直接写入 MySQL 数据库，不用消息队列
    @PostMapping("/api/user/signUp")
    public ResponseHandler insertUser(@RequestBody User user) throws BadRequestException {

        if (user.getUserName() == null) {
            throw new BadRequestException("用户名不能为空");
        }
        if (user.getPassword() == null) {
            throw new BadRequestException("密码不能为空");
        }
        if (user.getEmail() == null) {
            throw new BadRequestException("邮箱不能为空");
        }
        if (!user.getEmail().matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new BadRequestException("邮箱格式不正确");
        }
        userService.insertUser(user);
        return new ResponseHandler(ResponseHandler.SUCCESS,"注册成功");
    }

    //用户登录属于强一致性操作，不用消息队列
    @PostMapping("/api/user/login")
    public ResponseHandler Login(@RequestBody User login, HttpServletRequest request) throws BadRequestException {

        if (login.getUserName() == null || login.getPassword() == null)
            return new ResponseHandler(ResponseHandler.BAD_REQUEST, "用户名或密码不能为空");
        Long userId = userService.selectUserIdByNameAndPasswordAndReturnUserId(login);
        String token = JWTUtils.getJwtToken(userId);

        /**
         * 获取客户端IP地址和地理位置,存储到redis中
         */
        String ipAddress=GeoIpUtil.getClientIpAddress(request);
        String location=GeoIpUtil.getIpLocation(ipAddress);
        strRedisTemplate.opsForHash().put("UserIPLocation",String.valueOf(userId),location);

        return new ResponseHandler(ResponseHandler.SUCCESS, "登录成功", token);
    }
    //账号登出,直接删除redis中的token,不需要消息队列
    @PostMapping("/api/user/logout")
    public ResponseHandler Logout(@RequestHeader String token){
        JWTUtils.deleteToken(token);
        return new ResponseHandler(ResponseHandler.SUCCESS,"登出成功");
    }
}
