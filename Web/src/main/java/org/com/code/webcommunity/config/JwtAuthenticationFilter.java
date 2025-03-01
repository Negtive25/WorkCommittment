package org.com.code.webcommunity.config;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.com.code.webcommunity.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collection;
import java.util.Collections;

//SpringSecurity自定义的JWT认证过滤器，可以全局范围内拦截请求，并验证JWT令牌。
//实现这些 过滤层接口，然后SpringSecurity会一个一个按顺序的调用这些过滤层，把认证后的信息存储到
//UsernamePasswordAuthenticationToken对象中，调用其实现的接口AbstractAuthenticationToken的setAuthenticated(true)方法，
// 然后把认证的信息打包到SecurityContextHolder里
//因为setAuthenticated(true)，所以authenticated()方法返回true，所以才算通过了验证
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;  // 用户信息加载服务

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        //!!!!!!!!!!!!!!!!!!!!!!!!111
        //这一步一定要提前写好，因为JWT拦截器在SpringSecurity中的顺序比较前面
        // 所以即使你对一些url路径已经permitAll了，但是JWT依然会提前拦截所有permitAll的请求
        //出现403 Forbidden，所以这里要提前写好

        // 1. 排除登录路径
        String path = request.getServletPath();
        if (path.equals("/api/user/insertUser") ||
                path.equals("/api/articles/mostLikedArticles") ||
                path.equals("/api/articles/mostLatestArticles") ||
                path.equals("/api/articles/selectArticlesById") ||
                path.equals("/api/articles/selectArticlesLikeTitle") ||
                path.equals("/api/user/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 提取请求头中的JWT令牌
        String token = request.getHeader("token");

        // 3. 验证令牌有效性
        int userId=JWTUtils.checkToken(token);

        // 4. 如果令牌有效，则设置用户信息到SecurityContextHolder中

        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        if (userId >0) {
            //将JWT包含的用户的id信息打包到UserDetails中
            UserDetails userDetails = new User(String.valueOf(userId),"null",authorities);

            //将UserDetails信息打包到UsernamePasswordAuthenticationToken中
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, "null", authorities);

            //将UsernamePasswordAuthenticationToken信息打包到SecurityContextHolder中
            //SecurityContextHolder.getContext().setAuthentication()将完整的认证对象存储到线程绑定的安全上下文中。
            //此后在任何控制器或服务层，都能通过SecurityContextHolder获取当前用户身份
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }


        // 5. 继续执行后续过滤器链
        filterChain.doFilter(request, response);
    }
}