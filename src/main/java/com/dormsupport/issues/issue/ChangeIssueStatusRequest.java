package com.dormsupport.issues.issue;

import jakarta.validation.constraints.NotNull;

public record ChangeIssueStatusRequest(@NotNull IssueStatus status) {
}
