package com.dormsupport.issues.support;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.event.IssueEvent;
import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


public final class IssueTestFixtures {

    public static final UUID RESIDENT_ID =
            UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
    public static final UUID OTHER_RESIDENT_ID =
            UUID.fromString("cccccccc-cccc-4ccc-8ccc-cccccccccccc");
    public static final UUID REVIEWER_ID =
            UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
    public static final UUID BLOCK_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");
    public static final UUID OTHER_BLOCK_ID =
            UUID.fromString("dddddddd-dddd-4ddd-8ddd-dddddddddddd");

    public static final UUID PLUMBING_CATEGORY_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");
    public static final UUID ELECTRICITY_CATEGORY_ID =
            UUID.fromString("22222222-2222-4222-8222-222222222222");
    public static final UUID FURNITURE_CATEGORY_ID =
            UUID.fromString("33333333-3333-4333-8333-333333333333");

    public static final UUID NEW_ISSUE_ID =
            UUID.fromString("aaaaaaaa-0001-4000-8000-000000000001");
    public static final UUID IN_PROGRESS_ISSUE_ID =
            UUID.fromString("aaaaaaaa-0002-4000-8000-000000000002");
    public static final UUID CLOSED_ISSUE_ID =
            UUID.fromString("aaaaaaaa-0003-4000-8000-000000000003");

    private static final Instant FIXTURE_TIME = Instant.parse("2026-01-10T10:00:00Z");
    private static final UUID FIXTURE_EVENT_ID =
            UUID.fromString("ffffffff-ffff-4fff-8fff-ffffffffffff");

    private IssueTestFixtures() {
    }

    public static IssueCategory plumbingCategory() {
        return new IssueCategory("Plumbing", "Test category: plumbing");
    }

    public static IssueCategory electricityCategory() {
        return new IssueCategory("Electricity", "Test category: electricity");
    }

    public static IssueCategory furnitureCategory() {
        return new IssueCategory("Furniture", "Test category: furniture");
    }

    public static Issue issue() {
        return issue(RESIDENT_ID, BLOCK_ID, "101", plumbingCategory(),
                "Leaking tap", "The tap is leaking in the bathroom.", "Bathroom");
    }

    public static Issue issue(UUID residentId, UUID blockId, String roomNumber,
                              IssueCategory category, String title,
                              String description, String locationDetails) {
        return new Issue(residentId, blockId, roomNumber, title, description,
                category, locationDetails);
    }

    public static Authentication residentAuthentication() {
        return authentication(RESIDENT_ID, "RESIDENT");
    }

    public static Authentication otherResidentAuthentication() {
        return authentication(OTHER_RESIDENT_ID, "RESIDENT");
    }

    public static Authentication reviewerAuthentication() {
        return authentication(REVIEWER_ID, "REVIEWER");
    }

    public static Authentication adminAuthentication() {
        return authentication(UUID.fromString("99999999-9999-4999-8999-999999999999"), "ADMIN");
    }

    public static Authentication authentication(UUID userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    public static IssueEvent createdEvent(UUID issueId) {
        return new IssueEvent(
                FIXTURE_EVENT_ID,
                "issue.created",
                issueId,
                IssueStatus.NEW,
                null,
                FIXTURE_TIME,
                "Leaking tap",
                "The tap is leaking in the bathroom.",
                BLOCK_ID,
                "101",
                "Bathroom",
                "Plumbing");
    }

    public static IssueEvent statusChangedEvent(UUID issueId,
                                                IssueStatus previousStatus,
                                                IssueStatus status) {
        return new IssueEvent(
                FIXTURE_EVENT_ID,
                "issue.status_changed",
                issueId,
                status,
                previousStatus,
                FIXTURE_TIME,
                "Leaking tap",
                "The tap is leaking in the bathroom.",
                BLOCK_ID,
                "101",
                "Bathroom",
                "Plumbing");
    }
}
