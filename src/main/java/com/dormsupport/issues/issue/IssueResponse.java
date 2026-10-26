package com.dormsupport.issues.issue;

import com.dormsupport.issues.category.IssueCategory;

import java.time.Instant;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        UUID residentId,
        UUID blockId,
        String roomNumber,
        String title,
        String description,
        UUID categoryId,
        String categoryName,
        IssueStatus status,
        String locationDetails,
        Instant createdAt,
        Instant updatedAt) {

    public static IssueResponse from(Issue issue) {
        IssueCategory category = issue.getCategory();
        return new IssueResponse(
                issue.getId(),
                issue.getResidentId(),
                issue.getBlockId(),
                issue.getRoomNumber(),
                issue.getTitle(),
                issue.getDescription(),
                category.getId(),
                category.getName(),
                issue.getStatus(),
                issue.getLocationDetails(),
                issue.getCreatedAt(),
                issue.getUpdatedAt());
    }
}
