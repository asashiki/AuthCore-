package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: ASASHIKI
 * @CreateTime: 2025-03-27  22:37
 * @Description:
 */
@Component
public class JwtUtils {


    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate stringRedisTemplate; // Redis 组件，用于存储和检查 JWT 黑名单

    /**
     * 解析 JWT 令牌，并检查其是否有效（未过期、未被加入黑名单）。
     *
     * @param headerToken 从请求头获取的 Token（可能带有 "Bearer " 前缀）
     * @return 解析后的 DecodedJWT 对象，如果无效则返回 null
     */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken); // 解析去掉 "Bearer " 的实际 Token
        if (token == null) return null;

        Algorithm algorithm = Algorithm.HMAC256(key); // 使用 HMAC256 加密算法验证 Token
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try {
            DecodedJWT verify = jwtVerifier.verify(token); // 验证 Token 是否有效
            if (this.isInvalidToken(verify.getId())) // 检查 Token 是否在黑名单中（已注销）
                return null;

            Date expiresAt = verify.getExpiresAt(); // 获取 Token 过期时间
            return new Date().after(expiresAt) ? null : verify; // 如果 Token 过期，返回 null，否则返回解析后的 Token
        } catch (JWTVerificationException e) {
            throw null; // 发生 JWT 验证异常，抛出 null（可以改成日志或自定义异常）
        }
    }

    /**
     * 让 JWT 令牌失效（即加入黑名单）。
     *
     * @param headerToken 从请求头获取的 Token
     * @return 令牌是否成功加入黑名单（即是否成功注销）
     */
    public boolean invalidateJwt(String headerToken) {
        String token = this.convertToken(headerToken); // 解析 Token
        if (token == null) return false;

        Algorithm algorithm = Algorithm.HMAC256(key); // 解析 Token 所需的加密算法
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try {
            DecodedJWT jwt = jwtVerifier.verify(token); // 解析 Token
            String id = jwt.getId(); // 获取 JWT ID（唯一标识）
            return deleteToken(id, jwt.getExpiresAt()); // 将 Token 加入黑名单
        } catch (JWTVerificationException e) {
            return false; // 验证失败，Token 无效
        }
    }

    /**
     * 将 Token 加入 Redis 黑名单，防止被再次使用。
     *
     * @param uuid  JWT 的唯一 ID
     * @param time  JWT 的过期时间
     * @return 是否成功加入黑名单
     */
    private boolean deleteToken(String uuid, Date time) {
        if (this.isInvalidToken(uuid)) return false; // 如果已在黑名单，直接返回 false

        Date now = new Date();
        long expires = Math.max(time.getTime() - now.getTime(), 0); // 计算 Token 剩余的有效时间
        stringRedisTemplate.opsForValue().set(Const.JWT_BLACK_LIST + uuid, "", expires, TimeUnit.MILLISECONDS);
        return true; // 成功加入黑名单
    }

    /**
     * 检查 Token 是否已经失效（是否在黑名单中）。
     *
     * @param uuid JWT 的唯一 ID
     * @return 是否已失效
     */
    private boolean isInvalidToken(String uuid) {
        return stringRedisTemplate.hasKey(Const.JWT_BLACK_LIST + uuid); // 检查 Redis 黑名单
    }

    public String createJWT(UserDetails details, int id, String username) {
        Date expire = this.expireTime();
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }

    public Date expireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))  //???
                .build();
    }

    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    private String convertToken(String headerToken) {
        if (headerToken == null || headerToken.isEmpty() || !headerToken.startsWith("Bearer ")) {
            return null;
        }
        return headerToken.substring(7);
    }


}
