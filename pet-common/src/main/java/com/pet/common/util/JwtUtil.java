package com.pet.common.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    // 生成Token（业务用）
    public String generateToken(Long userId, String phone) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expire * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("phone", phone)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // 解析Token
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("token 已过期", e);
            return false;
        } catch (SignatureException e) {
            log.error("token 签名无效，密钥可能不匹配", e);
            return false;
        } catch (MalformedJwtException e) {
            log.error("token 格式错误", e);
            return false;
        } catch (Exception e) {
            log.error("token 验证其他异常", e);
            return false;
        }
    }

    // 从Token获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }
}