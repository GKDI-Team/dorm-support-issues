package com.dormsupport.issues.issue;

import com.dormsupport.issues.history.IssueStatusHistory;

import java.time.Instant;
import java.util.UUID;

public record StatusHistoryResponse(
        UUID id,
        IssueStatus fromStatus,
        IssueStatus toStatus,
        UUID changedBy,
        Instant changedAt) {

    public static StatusHistoryResponse from(IssueStatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedBy(),
                history.getChangedAt());
    }
}
