package com.example.tech_interview_buddy.dto.request;

import com.example.tech_interview_buddy.domain.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QuestionUpdateRequest {
    private String content;
    private Category category;
    private List<String> tags;
}
