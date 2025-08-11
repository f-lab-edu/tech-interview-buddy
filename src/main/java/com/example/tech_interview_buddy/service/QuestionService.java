package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Question findById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));
    }

    public Page<Question> findAllQuestions(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    public Page<Question> findByCategory(Category category, Pageable pageable) {
        return questionRepository.findByCategory(category, pageable);
    }

    public Page<Question> findByKeyword(String keyword, Pageable pageable) {
        return questionRepository.findByKeyword(keyword, pageable);
    }

    public Page<Question> findByTags(List<String> tagNames, Pageable pageable) {
        return questionRepository.findByTags(tagNames, pageable);
    }

    public Page<Question> findBySolvedStatus(Boolean isSolved, Pageable pageable) {
        return questionRepository.findByIsSolved(isSolved, pageable);
    }

    @Transactional
    public void markQuestionAsSolved(Long id) {
        Question question = findById(id);
        question.markAsSolved();
    }
}