package com.example.library.filter;

import com.example.library.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 如果没有 Authorization 头或不是 Bearer 格式，直接放行（后续 Security 会处理）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parseToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if (username != null && role != null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("用户 {} 认证成功，角色: {}", username, role);
            }
            // 继续过滤链
            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
            handleInvalidToken(response, "Token 已过期，请重新登录");
        } catch (SignatureException e) {
            log.warn("JWT 签名无效: {}", e.getMessage());
            handleInvalidToken(response, "Token 签名无效，请重新登录");
        } catch (MalformedJwtException e) {
            log.warn("JWT 格式错误: {}", e.getMessage());
            handleInvalidToken(response, "Token 格式错误，请重新登录");
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            handleInvalidToken(response, "Token 无效，请重新登录");
        }
    }

    /**
     * 统一处理无效 Token：返回 401 状态码，并写入 JSON 错误信息
     */
    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + message + "\",\"data\":null}");
        // 注意：这里没有调用 chain.doFilter()，因此请求被终止，不再继续向下传递
    }
}