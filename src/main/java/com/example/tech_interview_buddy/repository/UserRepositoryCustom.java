package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.User;

import java.util.List;


public interface UserRepositoryCustom {
    
    List<User> findUserByUsername(String username);
    
}
