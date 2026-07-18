package com.example.library.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // 交给Spring管理，方便其他地方注入
public class JwtUtil {
    // 生成一个安全的密钥（每次重启会变，实际项目建议放到配置文件中固定）
    private static final SecretKey KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 生成Token（有效期24小时）
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(KEY)
                .compact();
    }

    // 解析Token，获取Claims（如果过期或无效会抛异常）
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}