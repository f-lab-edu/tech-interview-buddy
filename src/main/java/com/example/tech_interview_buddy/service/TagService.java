package com.example.tech_interview_buddy.service;

import com.example.tech_interview_buddy.domain.Tag;
import com.example.tech_interview_buddy.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag createTag(String name, String description) {
        if (tagRepository.existsByName(name)) {
            throw new IllegalArgumentException("Tag already exists with name: " + name);
        }

        Tag tag = Tag.builder()
            .name(name)
            .description(description)
            .build();
        return tagRepository.save(tag);
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + id));
    }

    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name);
    }

    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }
}