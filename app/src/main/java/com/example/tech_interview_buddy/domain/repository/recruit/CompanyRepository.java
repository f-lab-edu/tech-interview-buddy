package com.example.tech_interview_buddy.domain.repository.recruit;

import com.example.tech_interview_buddy.domain.recruit.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);
}
