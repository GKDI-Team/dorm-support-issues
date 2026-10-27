package com.dormsupport.issues.event;

import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueStatus;

import java.time.Instant;
import java.util.UUID;

public record IssueEvent(
        UUID eventId,
        String eventType,
        UUID issueId,
        IssueStatus status,
        IssueStatus previousStatus,
        Instant occurredAt,
        String title,
        String descriptionPreview,
        UUID blockId,
        String roomNumber,
        String locationDetails,
        String categoryName) {

    public static IssueEvent created(Issue issue) {
        return new IssueEvent(UUID.randomUUID(), "issue.created", issue.getId(), issue.getStatus(), null,
                Instant.now(), issue.getTitle(), preview(issue.getDescription()), issue.getBlockId(),
                issue.getRoomNumber(), issue.getLocationDetails(), issue.getCategory().getName());
    }

    public static IssueEvent statusChanged(Issue issue, IssueStatus previousStatus) {
        return new IssueEvent(UUID.randomUUID(), "issue.status_changed", issue.getId(), issue.getStatus(),
                previousStatus, Instant.now(), issue.getTitle(), preview(issue.getDescription()), issue.getBlockId(),
                issue.getRoomNumber(), issue.getLocationDetails(), issue.getCategory().getName());
    }

    private static String preview(String description) {
        return description.length() <= 500 ? description : description.substring(0, 500);
    }
}
