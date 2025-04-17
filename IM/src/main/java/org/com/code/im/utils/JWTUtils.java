package org.com.code.im.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import org.com.code.im.exception.ResourceNotFoundException;
import org.com.code.im.mapper.UserMapper;
import org.com.code.im.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtils {
    private final static String jwtSecret = "abcdefghijklmnopqrstuvwxyz123456789123456789";

    private static RedisTemplate<String, Long> redisTemplate;

    public JWTUtils(@Qualifier("redisTemplate") RedisTemplate<String, Long> redisTemplate) {
        JWTUtils.redisTemplate = redisTemplate;
    }
    private static StringBuffer jwtSecretBuffer= new StringBuffer();


    public static String getJwtToken(long userId){
        if(redisTemplate.hasKey("Auth"+userId))
            redisTemplate.delete("Auth"+userId);

        Map<String, String> claimsOfRandomDigit= new HashMap<>();

        String random = (Math.random() * 100000000)+"";

        claimsOfRandomDigit.put("random", random);

        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .setClaims(claimsOfRandomDigit)
                .compact();

        String authentication = "Auth"+userId;

        Map<String, Long> hash = new HashMap<>();
        hash.put(token,userId);
        redisTemplate.opsForHash().putAll(authentication, hash);

        redisTemplate.expire(authentication, Duration.ofDays(14));

        jwtSecretBuffer.setLength(0);
        jwtSecretBuffer.append("Auth").append(userId).append(" ").append(token);
        return jwtSecretBuffer.toString();
    }

    public static Long checkToken(String token) {
        String[] parts = token.split(" ");
        if (parts.length != 2) {
            throw new ResourceNotFoundException("身份验证失败，token格式无效"+token);
        }

        if (redisTemplate.opsForHash().hasKey(parts[0], parts[1])) {
            Object userIdObj = redisTemplate.opsForHash().get(parts[0], parts[1]);

            return Long.parseLong(userIdObj.toString());
        } else {
            throw new ResourceNotFoundException("请登录");
        }
    }

    public static void deleteToken(String token) {
        String[] parts = token.split(" ");
        if (parts.length != 2) {
            throw new ResourceNotFoundException("token格式无效,登出失败");
        }
        if(redisTemplate.opsForHash().hasKey(parts[0], parts[1]))
            redisTemplate.delete(parts[0]);
        else
            throw new ResourceNotFoundException("token无效,登出失败");
    }
}
