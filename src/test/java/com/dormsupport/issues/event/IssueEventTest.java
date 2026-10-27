package com.dormsupport.issues.event;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IssueEventTest {

    @Test
    void created_shouldContainIssueDataAndGenerateEventId() {
        Issue issue = createIssue("Broken pipe");

        IssueEvent event = IssueEvent.created(issue);

        assertNotNull(event.eventId());
        assertEquals("issue.created", event.eventType());
        assertEquals(issue.getId(), event.issueId());
        assertEquals(issue.getStatus(), event.status());
        assertNull(event.previousStatus());
        assertNotNull(event.occurredAt());
        assertEquals(issue.getTitle(), event.title());
        assertEquals(issue.getDescription(), event.descriptionPreview());
        assertEquals(issue.getBlockId(), event.blockId());
        assertEquals(issue.getRoomNumber(), event.roomNumber());
        assertEquals(issue.getLocationDetails(), event.locationDetails());
        assertEquals(issue.getCategory().getName(), event.categoryName());
    }

    @Test
    void statusChanged_shouldContainCurrentAndPreviousStatuses() {
        Issue issue = createIssue("Broken pipe");
        IssueStatus previousStatus = IssueStatus.NEW;

        when(issue.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        IssueEvent event = IssueEvent.statusChanged(issue, previousStatus);

        assertNotNull(event.eventId());
        assertEquals("issue.status_changed", event.eventType());
        assertEquals(issue.getId(), event.issueId());
        assertEquals(IssueStatus.IN_PROGRESS, event.status());
        assertEquals(IssueStatus.NEW, event.previousStatus());
        assertNotNull(event.occurredAt());
        assertEquals(issue.getTitle(), event.title());
        assertEquals(issue.getDescription(), event.descriptionPreview());
        assertEquals(issue.getBlockId(), event.blockId());
        assertEquals(issue.getRoomNumber(), event.roomNumber());
        assertEquals(issue.getLocationDetails(), event.locationDetails());
        assertEquals(issue.getCategory().getName(), event.categoryName());
    }

    @Test
    void created_shouldGenerateUniqueEventIds() {
        Issue issue = createIssue("Broken pipe");

        IssueEvent firstEvent = IssueEvent.created(issue);
        IssueEvent secondEvent = IssueEvent.created(issue);

        assertNotEquals(firstEvent.eventId(), secondEvent.eventId());
    }

    @Test
    void created_shouldNotTruncateDescriptionOfExactly500Characters() {
        String description = "a".repeat(500);
        Issue issue = createIssue(description);

        IssueEvent event = IssueEvent.created(issue);

        assertEquals(description, event.descriptionPreview());
        assertEquals(500, event.descriptionPreview().length());
    }

    @Test
    void created_shouldTruncateDescriptionLongerThan500Characters() {
        String description = "a".repeat(501);
        Issue issue = createIssue(description);

        IssueEvent event = IssueEvent.created(issue);

        assertEquals(description.substring(0, 500), event.descriptionPreview());
        assertEquals(500, event.descriptionPreview().length());
    }

    @Test
    void created_shouldKeepShortDescriptionUnchanged() {
        String description = "Water is leaking from the ceiling.";
        Issue issue = createIssue(description);

        IssueEvent event = IssueEvent.created(issue);

        assertEquals(description, event.descriptionPreview());
    }

    private Issue createIssue(String description) {
        Issue issue = mock(Issue.class);
        IssueCategory category = mock(IssueCategory.class);

        when(issue.getId()).thenReturn(UUID.randomUUID());
        when(issue.getStatus()).thenReturn(IssueStatus.NEW);
        when(issue.getTitle()).thenReturn("Broken pipe");
        when(issue.getDescription()).thenReturn(description);
        when(issue.getBlockId()).thenReturn(UUID.randomUUID());
        when(issue.getRoomNumber()).thenReturn("101");
        when(issue.getLocationDetails()).thenReturn("Bathroom");
        when(issue.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn("Plumbing");

        return issue;
    }
}