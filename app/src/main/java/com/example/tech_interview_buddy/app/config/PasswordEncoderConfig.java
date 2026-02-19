package com.example.tech_interview_buddy.app.config;

import com.example.tech_interview_buddy.common.security.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * PasswordEncoder 구현체 제공
 * Common 인터페이스를 구현하여 Domain에서 사용
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoderAdapter(new BCryptPasswordEncoder());
    }
    
    private static class PasswordEncoderAdapter implements PasswordEncoder {
        private final org.springframework.security.crypto.password.PasswordEncoder delegate;
        
        public PasswordEncoderAdapter(org.springframework.security.crypto.password.PasswordEncoder delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public String encode(CharSequence rawPassword) {
            return delegate.encode(rawPassword);
        }
        
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return delegate.matches(rawPassword, encodedPassword);
        }
    }
}

