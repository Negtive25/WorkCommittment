package org.com.code.im.config;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.com.code.im.mapper.UserMapper;
import org.com.code.im.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collection;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserMapper userMapper;

    @Autowired
    public JwtAuthenticationFilter(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    //使用filter中的init()方法来在filter的生命周期中我们手动注入需要使用的Service；

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        // 1. 提取请求头中的JWT令牌
        String token = request.getHeader("token");

        // 2. 验证令牌有效性
        long userId=JWTUtils.checkToken(token);

        // 3. 如果令牌有效，则设置用户信息到SecurityContextHolder中
        try{
            //从数据库中查询用户的权限信息,然后将它存储到Security的上下文中
            Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(userMapper.getAuth(userId)));

            //将JWT包含的用户的id信息打包到UserDetails中
            UserDetails userDetails = new User(String.valueOf(userId),"null",authorities);

            //将UserDetails信息打包到UsernamePasswordAuthenticationToken中
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, "null", authorities);

            //将UsernamePasswordAuthenticationToken信息打包到SecurityContextHolder中
            //SecurityContextHolder.getContext().setAuthentication()将完整的认证对象存储到线程绑定的安全上下文中。
            //此后在任何控制器或服务层，都能通过SecurityContextHolder获取当前用户身份
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (Exception e){
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 4. 继续执行后续过滤器链
        filterChain.doFilter(request, response);
    }
}