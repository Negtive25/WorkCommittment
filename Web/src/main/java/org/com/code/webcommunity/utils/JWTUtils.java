package org.com.code.webcommunity.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.com.code.webcommunity.exception.ResourceNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtils {
    private final static String jwtSecret = "abcdefghijklmnopqrstuvwxyz123456789123456789";

    private static RedisTemplate<String, Integer> redisTemplate;
    private static StringBuffer jwtSecretBuffer= new StringBuffer();

    public JWTUtils(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static String getJwtToken(int userId){
        if(redisTemplate.hasKey("Auth"+userId))
            redisTemplate.delete("Auth"+userId);

        Map<String, Object> claimsOfRandomDigit= new HashMap<>();
        Map<String, Object> claimsOfUserId= new HashMap<>();

        int random = (int)(Math.random()*100000000);

        claimsOfRandomDigit.put("random", random);
        claimsOfUserId.put("userId", userId);

        String tokenOfRandomDigit = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .setClaims(claimsOfRandomDigit)
                .compact();

        String token= tokenOfRandomDigit;

        String authentication = "Auth"+userId;

        Map<String, Object> hash = new HashMap<>();
        hash.put(token, userId);
        redisTemplate.opsForHash().putAll(authentication, hash);

        redisTemplate.expire(authentication, Duration.ofDays(14));

        jwtSecretBuffer.setLength(0);
        jwtSecretBuffer.append("Auth").append(userId).append(" ").append(token);
        return jwtSecretBuffer.toString();
    }

    public static int checkToken(String token) {
        String[] parts = token.split(" ");
        if (parts.length != 2) {
            throw new ResourceNotFoundException("身份验证失败，token格式无效"+token);
        }

        if (redisTemplate.opsForHash().hasKey(parts[0], parts[1])) {
            Object userIdObj = redisTemplate.opsForHash().get(parts[0], parts[1]);

            return (Integer) userIdObj;
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
