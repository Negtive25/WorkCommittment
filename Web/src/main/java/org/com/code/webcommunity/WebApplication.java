package org.com.code.webcommunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
@EnableScheduling
public class WebApplication implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    //!!!!!!!!!!!!!!!!!!!!!!!!
    // 在项目启动时执行
    //这是在测试本地用idea连接远程服务器的redis库
    //如果连接成功，并且redis库的ping键值没有对应的值，则设置ping键值对为pong
    //然后在云端的redis库中就可以读取到pong
    //如果没有连接到redis库，则打印错误信息
    @Override
    public void run(String... args) throws Exception {
        try {
            String value = redisTemplate.opsForValue().get("ping");
            if (value == null) {
                redisTemplate.opsForValue().set("ping", "pong");
            }
        } catch (Exception e) {
            // 处理异常并返回错误响应（如果需要）
            System.err.println("Error setting ping: " + e.getMessage());
        }
    }
}
