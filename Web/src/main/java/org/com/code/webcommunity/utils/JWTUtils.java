package org.com.code.webcommunity.utils;


import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.com.code.webcommunity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtils {
    private final static String jwtSecret = "abcdefghijklmnopqrstuvwxyz123456789123456789";
    private static RedisTemplate<String, Integer> redisTemplate;

    public JWTUtils(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static String getJwtToken(int userId){
        Map<String, Object> claims= new HashMap<>();
        claims.put("userId", userId);
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .setClaims(claims)
                .compact();

        redisTemplate.opsForValue().set("token_"+token, userId);
        redisTemplate.expire("token_"+token, Duration.ofDays(14));

        return token;
    }

    public static int checkToken(String token) {
        if(redisTemplate.hasKey("token_"+token)){
            return redisTemplate.opsForValue().get("token_"+token);
        }else {
            throw new ResourceNotFoundException("身份验证失败，token无效");
        }
    }

    public static void deleteToken(String token) {
        redisTemplate.delete("token_"+token);
    }
}
