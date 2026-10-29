package com.dormsupport.issues.category;

import java.util.UUID;

public record IssueCategoryResponse(
        UUID id,
        String name,
        String description) {

    public static IssueCategoryResponse from(IssueCategory category) {
        return new IssueCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }
}
