// TODO: Test the allowed status transitions: NEW -> IN_PROGRESS -> CLOSED.
// TODO: Test that repeated, backward and CLOSED transitions are rejected.

package com.dormsupport.issues.issue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IssueStatusTest {

    @Test
    void shouldAllowTransitionFromNewToInProgress() {
        assertTrue(IssueStatus.NEW.canTransitionTo(IssueStatus.IN_PROGRESS));
    }

    @Test
    void shouldAllowTransitionFromInProgressToClosed() {
        assertTrue(IssueStatus.IN_PROGRESS.canTransitionTo(IssueStatus.CLOSED));
    }

    @Test
    void shouldRejectTransitionFromNewToClosed() {
        assertFalse(IssueStatus.NEW.canTransitionTo(IssueStatus.CLOSED));
    }

    @Test
    void shouldRejectTransitionFromInProgressToNew() {
        assertFalse(IssueStatus.IN_PROGRESS.canTransitionTo(IssueStatus.NEW));
    }

    @Test
    void shouldRejectTransitionFromClosedToNew() {
        assertFalse(IssueStatus.CLOSED.canTransitionTo(IssueStatus.NEW));
    }

    @Test
    void shouldRejectTransitionFromClosedToInProgress() {
        assertFalse(IssueStatus.CLOSED.canTransitionTo(IssueStatus.IN_PROGRESS));
    }

    @Test
    void shouldRejectTransitionToTheSameStatus() {
        for (IssueStatus status : IssueStatus.values()) {
            assertFalse(status.canTransitionTo(status));
        }
    }
}
