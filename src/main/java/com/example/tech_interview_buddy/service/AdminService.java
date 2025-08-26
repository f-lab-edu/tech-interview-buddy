package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Question;
import com.example.tech_interview_buddy.domain.Tag;
import com.example.tech_interview_buddy.domain.User;
import com.example.tech_interview_buddy.domain.UserRole;
import com.example.tech_interview_buddy.dto.request.QuestionCreateRequest;
import com.example.tech_interview_buddy.dto.request.QuestionUpdateRequest;
import com.example.tech_interview_buddy.dto.request.TagRequest;
import com.example.tech_interview_buddy.dto.response.QuestionDetailResponse;
import com.example.tech_interview_buddy.dto.response.UserResponse;
import com.example.tech_interview_buddy.repository.QuestionRepository;
import com.example.tech_interview_buddy.repository.TagRepository;
import com.example.tech_interview_buddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public QuestionDetailResponse createQuestion(QuestionCreateRequest request) {
        Question question = Question.builder()
                .content(request.getContent())
                .category(request.getCategory())
                .build();

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> tags = request.getTags().stream()
                    .map(tagName -> findOrCreateTag(tagName))
                    .collect(Collectors.toList());
            question.setTags(tags);
        }

        Question savedQuestion = questionRepository.save(question);
        return QuestionDetailResponse.from(savedQuestion);
    }

    public QuestionDetailResponse updateQuestion(Long questionId, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        question.updateContent(request.getContent());
        question.updateCategory(request.getCategory());

        if (request.getTags() != null) {
            List<Tag> tags = request.getTags().stream()
                    .map(tagName -> findOrCreateTag(tagName))
                    .collect(Collectors.toList());
            question.setTags(tags);
        }

        return QuestionDetailResponse.from(question);
    }

    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Question not found with id: " + questionId);
        }
        questionRepository.deleteById(questionId);
    }

    public void addTagToQuestion(Long questionId, TagRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        Tag tag = findOrCreateTag(request.getName());
        question.addTag(tag);
    }

    public void removeTagFromQuestion(Long questionId, String tagName) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        question.removeTag(tagName);
    }

    public UserResponse grantAdminRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        user.updateRole(UserRole.ADMIN);
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    public UserResponse revokeAdminRole(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        user.updateRole(UserRole.USER);
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build())
                .collect(Collectors.toList());
    }

    private Tag findOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder().name(tagName).build();
                    return tagRepository.save(newTag);
                });
    }
}
