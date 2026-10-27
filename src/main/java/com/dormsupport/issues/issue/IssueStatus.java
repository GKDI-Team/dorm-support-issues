package com.dormsupport.issues.issue;

public enum IssueStatus {
    NEW,
    IN_PROGRESS,
    CLOSED;

    public boolean canTransitionTo(IssueStatus target) {
        return switch (this) {
            case NEW -> target == IN_PROGRESS;
            case IN_PROGRESS -> target == CLOSED;
            case CLOSED -> false;
        };
    }
}
