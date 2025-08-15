package com.example.tech_interview_buddy.dto.request;

import com.example.tech_interview_buddy.domain.Category;

import java.util.List;

public class QuestionSearchRequest {
    
    private Category category;
    private String keyword;
    private List<String> tags;
    private Boolean isSolved;
    private int page = 0;
    private int size = 20;
    private String sort = "id";
    private String direction = "asc";

    public QuestionSearchRequest() {}

    public QuestionSearchRequest(Category category, String keyword, List<String> tags, 
                               Boolean isSolved, int page, int size, String sort, String direction) {
        this.category = category;
        this.keyword = keyword;
        this.tags = tags;
        this.isSolved = isSolved;
        this.page = page;
        this.size = size;
        this.sort = sort;
        this.direction = direction;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getIsSolved() {
        return isSolved;
    }

    public void setIsSolved(Boolean isSolved) {
        this.isSolved = isSolved;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
} 