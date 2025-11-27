package com.example.tech_interview_buddy.app.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.data.domain.Sort;


public enum SortDirection {
    ASC("asc"),
    DESC("desc");

    private final String direction;

    SortDirection(String direction) {
        this.direction = direction;
    }

    @JsonValue
    public String getDirection() {
        return direction;
    }

    public Sort.Direction toSortDirection() {
        return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    @JsonCreator
    public static SortDirection fromString(String direction) {
        if (direction == null) {
            return ASC;
        }

        for (SortDirection sortDir : SortDirection.values()) {
            if (sortDir.direction.equalsIgnoreCase(direction)) {
                return sortDir;
            }
        }
        return ASC;
    }
}
