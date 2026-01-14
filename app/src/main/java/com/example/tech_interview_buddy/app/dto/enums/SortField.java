package com.example.tech_interview_buddy.app.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum SortField {
    ID("id"),
    CONTENT("content"),
    CATEGORY("category"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt"),
    IS_SOLVED("isSolved");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    @JsonValue
    public String getFieldName() {
        return fieldName;
    }

    @JsonCreator
    public static SortField fromString(String sort) {
        if (sort == null) {
            return ID;
        }

        for (SortField field : SortField.values()) {
            if (field.fieldName.equalsIgnoreCase(sort)) {
                return field;
            }
        }
        return ID;
    }
}
