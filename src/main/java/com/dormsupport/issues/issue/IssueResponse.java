package com.dormsupport.issues.issue;

import com.dormsupport.issues.category.IssueCategory;

import java.time.Instant;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        UUID residentId,
        String block,
        String roomNumber,
        String title,
        String description,
        UUID categoryId,
        String categoryName,
        IssueStatus status,
        String locationDetails,
        Instant createdAt,
        Instant updatedAt) {

    /** Compatibility constructor for existing test fixtures. */
    public IssueResponse(UUID id, UUID residentId, UUID blockId, String roomNumber,
                         String title, String description, UUID categoryId,
                         String categoryName, IssueStatus status, String locationDetails,
                         Instant createdAt, Instant updatedAt) {
        this(id, residentId, blockId == null ? null : blockId.toString(), roomNumber,
                title, description, categoryId, categoryName, status, locationDetails,
                createdAt, updatedAt);
    }

    /** Compatibility accessor for old API consumers and tests. */
    @Deprecated
    public UUID blockId() {
        try {
            return block == null ? null : UUID.fromString(block);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static IssueResponse from(Issue issue) {
        IssueCategory category = issue.getCategory();
        return new IssueResponse(
                issue.getId(),
                issue.getResidentId(),
                issue.getBlock(),
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
