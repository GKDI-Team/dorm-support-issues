package com.dormsupport.issues.issue;

import com.dormsupport.issues.history.IssueStatusHistory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IssueDetailsResponse(
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
        Instant updatedAt,
        List<StatusHistoryResponse> statusHistory) {

    /** Compatibility constructor for existing test fixtures. */
    public IssueDetailsResponse(UUID id, UUID residentId, UUID blockId, String roomNumber,
                                String title, String description, UUID categoryId,
                                String categoryName, IssueStatus status, String locationDetails,
                                Instant createdAt, Instant updatedAt,
                                List<StatusHistoryResponse> statusHistory) {
        this(id, residentId, blockId == null ? null : blockId.toString(), roomNumber,
                title, description, categoryId, categoryName, status, locationDetails,
                createdAt, updatedAt, statusHistory);
    }

    public static IssueDetailsResponse from(Issue issue, List<IssueStatusHistory> history) {
        return new IssueDetailsResponse(
                issue.getId(),
                issue.getResidentId(),
                issue.getBlock(),
                issue.getRoomNumber(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getCategory().getId(),
                issue.getCategory().getName(),
                issue.getStatus(),
                issue.getLocationDetails(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                history.stream().map(StatusHistoryResponse::from).toList());
    }
}
