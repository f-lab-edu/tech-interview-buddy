package com.example.tech_interview_buddy.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.example.tech_interview_buddy.app",
	"com.example.tech_interview_buddy.domain",
	"com.example.tech_interview_buddy.common"
})
public class TechInterviewBuddyApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechInterviewBuddyApplication.class, args);
	}

}
