package com.example.tech_interview_buddy.repository;

import com.example.tech_interview_buddy.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword%")
    List<Tag> findByNameContaining(@Param("keyword") String keyword);
}
