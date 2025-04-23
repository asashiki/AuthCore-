package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-03-28  00:01
 * @Description:
 */
@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    //JWTVerifier 解析JWT
    //DecodedJWT 解析JWT
    @Resource
    JwtUtils jwtUtils;

    @Override
    //验证了请求头中Authorization的Token并将其包装为认证信息:UsernamePasswordAuthenticationToken
    //将用户的ID存储到请求
    // 属性中
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        DecodedJWT jwt = jwtUtils.resolveJwt(authorization);
        if (jwt != null) {
            UserDetails user = jwtUtils.toUser(jwt);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));  //????
            //将用户的认证信息存储到 SecurityContextHolder 的全局的上下文管理器中，从而允许在应用的任何地方访问和操作当前用户的身份认证信息
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.setAttribute("id",jwtUtils.toId(jwt));
        }
        filterChain.doFilter(request, response);//直接往下走
    }
}
