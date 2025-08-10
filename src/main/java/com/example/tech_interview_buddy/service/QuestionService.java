package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.Category;
import com.example.tech_interview_buddy.domain.Tag;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import com.example.tech_interview_buddy.repository.TagRepository;
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
    private final TagRepository tagRepository;
    
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
    public Question createQuestion(String title, String content, Category category, List<String> tagNames) {
        Question question = Question.builder()
            .content(content)
            .category(category)
            .build();
        
        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                question.addTag(tag);
            }
        }
        
        return questionRepository.save(question);
    }
    
    @Transactional
    public void markQuestionAsSolved(Long id) {
        Question question = findById(id);
        question.markAsSolved();
    }
    
    @Transactional
    public void markQuestionAsUnsolved(Long id) {
        Question question = findById(id);
        question.markAsUnsolved();
    }
    
    @Transactional
    public void addTagToQuestion(Long questionId, String tagName) {
        Question question = findById(questionId);
        Tag tag = tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
        question.addTag(tag);
    }
    
    @Transactional
    public void removeTagFromQuestion(Long questionId, String tagName) {
        Question question = findById(questionId);
        Tag tag = tagRepository.findByName(tagName)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagName));
        question.removeTag(tag);
    }
} 