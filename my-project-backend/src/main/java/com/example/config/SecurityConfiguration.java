package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JwtAuthorizeFilter;
import com.example.service.AccountService;
import com.example.utils.JwtUtils;
import com.fasterxml.jackson.databind.util.BeanUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-03-27  19:51
 * @Description:
 */
@Configuration
public class SecurityConfiguration {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountService accountService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 配置授权规则
                .authorizeHttpRequests(conf -> conf
                        // 允许所有用户访问 `/api/auth/**` 相关的 API（例如注册、登录等）
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        // 其他所有请求都需要身份验证
                        .anyRequest().authenticated()
                )
                // 配置表单登录
                .formLogin(conf -> conf
                        // 处理登录请求的接口（前端需要发送 POST 请求到该地址进行登录）
                        .loginProcessingUrl("/api/auth/login")
                        // 登录成功时的处理逻辑
                        .successHandler(this::onAuthenticationSuccess)
                        // 登录失败时的处理逻辑
                        .failureHandler(this::onAuthenticationFailure)
                )
                // 配置登出功能
                .logout(conf -> conf
                        // 处理登出的接口（前端请求该地址可登出）
                        .logoutUrl("/api/auth/logout")
                        // 登出成功时的处理逻辑
                        .logoutSuccessHandler(this::onLogoutSuccess)
                )
                // 关闭 CSRF 保护（适用于无状态 API，比如前后端分离的项目）
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理策略
                .sessionManagement(conf -> conf
                        // 设置为无状态会话（不在服务器端存储用户会话信息）
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 构建 SecurityFilterChain 实例
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                //
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized)
                        .accessDeniedHandler(this::onAccessDeny)
                )
                .build();
    }


    public void onUnauthorized(HttpServletRequest request,
                               HttpServletResponse response,
                               AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.unauthorized(authException.getMessage()).asJsonString());
    }

    //没登陆是没认证啊，权限不一样才403
    public void onAccessDeny (HttpServletRequest request,
                                     HttpServletResponse response,
                                     AccessDeniedException Exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.forbidden(Exception.getMessage()).asJsonString());
    }

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        User user = (User) authentication.getPrincipal();

        Account account = accountService.findAccountByUsernameOrEmail(user.getUsername());

        String token = jwtUtils.createJWT(user, account.getId(), account.getUsername());

        //AuthorizeVO authorizeVO = new AuthorizeVO();
        AuthorizeVO authorizeVO = account.asViewObject(AuthorizeVO.class, v -> {
            v.setExpire(jwtUtils.expireTime());
            v.setToken(token);
        });
        //BeanUtils.copyProperties(account, authorizeVO); //Beanutils spring自带小工具
        //authorizeVO.setExpire(jwtUtils.expireTime());
        //authorizeVO.setToken(token);
        //authorizeVO.setRole(account.getRole());
        //authorizeVO.setUsername(account.getUsername());

        response.getWriter().write(RestBean.success(authorizeVO).asJsonString());
    }

    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.unauthorized(exception.getMessage()).asJsonString());
    }

    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");
        if (jwtUtils.invalidateJwt(authorization)) {
            writer.write(RestBean.success().asJsonString());
        }else {
            writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
        }
    }

}

