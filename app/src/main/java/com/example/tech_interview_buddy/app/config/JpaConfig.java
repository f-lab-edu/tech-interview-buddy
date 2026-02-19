package com.example.tech_interview_buddy.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.example.tech_interview_buddy.domain.repository")
@EntityScan(basePackages = "com.example.tech_interview_buddy.domain")
public class JpaConfig {}

