package org.com.code.webcommunity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .formLogin(form -> form.disable())//禁用默认登录页面
                .logout(config->config.disable())//禁用默认登出页面
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable()) // 禁用 CSRF 保护（仅在开发环境中使用，生产环境应启用）
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 允许所有 OPTIONS 请求
                        .requestMatchers("/api/user/insertUser").permitAll()
                        .requestMatchers("/api/articles/mostLikedArticles").permitAll()
                        .requestMatchers("/api/articles/mostLatestArticles").permitAll()
                        .requestMatchers("/api/articles/selectArticlesById").permitAll()
                        .requestMatchers("/api/articles/selectArticlesLikeTitle").permitAll()
                        .requestMatchers("/api/user/login").permitAll() // 确保登录页面可访问
                        .anyRequest().authenticated() // 其他所有请求都需要认证
                )
                //添加JWT认证过滤器
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);;


        return http.build();
    }
}
