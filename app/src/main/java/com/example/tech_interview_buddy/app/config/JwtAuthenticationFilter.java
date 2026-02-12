package com.example.tech_interview_buddy.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.service.UserService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals("/api/v1/users/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (hasInvalidJwtToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            
            String jwt = getJwtFromRequest(request);
            long jwtExtractTime = System.currentTimeMillis();
            log.debug("JWT 추출 시간: {}ms", jwtExtractTime - startTime);
            
            String username = jwtTokenProvider.getUsernameFromToken(jwt);
            long usernameExtractTime = System.currentTimeMillis();
            log.debug("사용자명 추출 시간: {}ms", usernameExtractTime - jwtExtractTime);
            
            User user = userService.findByUsernameOrEmail(username);
            long userEntityTime = System.currentTimeMillis();
            log.debug("User 엔티티 로딩 시간: {}ms", userEntityTime - usernameExtractTime);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("currentUser", user);
            
            long totalTime = System.currentTimeMillis();
            log.debug("JWT 인증 총 시간: {}ms", totalTime - startTime);

        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasInvalidJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
            return true;
        }

        String jwt = bearerToken.substring(BEARER_PREFIX_LENGTH);
        return jwt == null || !jwtTokenProvider.validateToken(jwt);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return bearerToken.substring(BEARER_PREFIX_LENGTH);
    }
}
