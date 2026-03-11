package com.learn.knowledgesystem.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.learn.knowledgesystem.security.UserPrincipal;
import com.learn.knowledgesystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService; // 实际是 CustomUserDetailsService

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

//        System.out.println("JwtFilter 处理请求: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");

//        System.out.println("Authorization header: " + authHeader);

        String token = jwtUtil.getTokenFromHeader(authHeader);
        String username = null;
        Long userId = null;

        if (token != null) {
            DecodedJWT decoded = jwtUtil.validateAndGetDecodedJWT(token);
            if (decoded != null) {
                username = decoded.getSubject();
                userId = decoded.getClaim("userId").asLong();
                logger.debug("Token valid: username={}, userId={}", username, userId);
            } else {
                logger.debug("Invalid token: {}", token);
            }
        }

        if (username != null && userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 直接构造 UserPrincipal（如果不需要从数据库加载权限）
            UserPrincipal principal = new UserPrincipal(userId, username, null, Collections.emptyList());
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            logger.info("Authentication set for user: {}", username);
        }

        filterChain.doFilter(request, response);
    }
}