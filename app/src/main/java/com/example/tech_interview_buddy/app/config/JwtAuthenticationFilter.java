package com.example.tech_interview_buddy.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.example.tech_interview_buddy.domain.repository.UserRepository;
import com.example.tech_interview_buddy.domain.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

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
            
            // User 엔티티를 먼저 조회 (한 번만 DB 조회)
            User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            long userEntityTime = System.currentTimeMillis();
            log.debug("User 엔티티 로딩 시간: {}ms", userEntityTime - usernameExtractTime);
            
            // UserDetails는 User 엔티티에서 직접 생성 (DB 조회 없음)
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
            long userDetailsTime = System.currentTimeMillis();
            log.debug("UserDetails 생성 시간: {}ms", userDetailsTime - userEntityTime);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
            
            // User 엔티티를 SecurityContext에 추가로 저장
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
