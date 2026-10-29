package com.dormsupport.issues.issue;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.category.IssueCategoryRepository;
import com.dormsupport.issues.client.AuthServiceClient;
import com.dormsupport.issues.client.AuthUserProfile;
import com.dormsupport.issues.event.IssueEventPublisher;
import com.dormsupport.issues.history.IssueStatusHistoryRepository;
import com.dormsupport.issues.support.IssueTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueCategoryRepository categoryRepository;

    @Mock
    private IssueStatusHistoryRepository historyRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private IssueEventPublisher issueEventPublisher;

    private IssueService issueService;

    @BeforeEach
    void setUp() {
        issueService = new IssueService(
                issueRepository,
                categoryRepository,
                historyRepository,
                authServiceClient,
                issueEventPublisher
        );
    }

    @Test
    void create_shouldCreateIssueAndPublishEvent() {
        Authentication authentication =
                IssueTestFixtures.residentAuthentication();

        IssueCategory category = IssueTestFixtures.plumbingCategory();

        CreateIssueRequest request = new CreateIssueRequest(
                "  Leaking tap  ",
                "  The tap is leaking.  ",
                IssueTestFixtures.PLUMBING_CATEGORY_ID,
                "  Bathroom  "
        );

        when(authServiceClient.getUserProfile(IssueTestFixtures.RESIDENT_ID))
                .thenReturn(validResidentProfile());

        when(categoryRepository.findById(
                IssueTestFixtures.PLUMBING_CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IssueResponse response = issueService.create(request, authentication);

        assertEquals(IssueStatus.NEW, response.status());
        assertEquals("Leaking tap", response.title());
        assertEquals("The tap is leaking.", response.description());
        assertEquals("Bathroom", response.locationDetails());
        assertEquals(IssueTestFixtures.RESIDENT_ID, response.residentId());
        assertEquals(IssueTestFixtures.BLOCK_ID, response.blockId());
        assertEquals("101", response.roomNumber());

        verify(historyRepository).save(any());
        verify(issueEventPublisher).publishCreated(any(Issue.class));
    }

    @Test
    void create_shouldRejectUserWhoIsNotResident() {
        Authentication authentication =
                IssueTestFixtures.reviewerAuthentication();

        CreateIssueRequest request = validCreateRequest();

        assertThrows(
                IssueStatusChangeNotAllowedException.class,
                () -> issueService.create(request, authentication)
        );

        verify(authServiceClient, never()).getUserProfile(any());
        verify(issueRepository, never()).save(any());
        verify(issueEventPublisher, never()).publishCreated(any());
    }

    @Test
    void create_shouldRejectIncompleteResidentProfile() {
        Authentication authentication =
                IssueTestFixtures.residentAuthentication();

        when(authServiceClient.getUserProfile(IssueTestFixtures.RESIDENT_ID))
                .thenReturn(new AuthUserProfile(
                        IssueTestFixtures.RESIDENT_ID,
                        "resident",
                        "Test Resident",
                        "RESIDENT",
                        (String) null,
                        "101",
                        Instant.parse("2026-01-10T10:00:00Z")
                ));

        assertThrows(
                ResidentProfileIncompleteException.class,
                () -> issueService.create(validCreateRequest(), authentication)
        );

        verify(issueRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(issueEventPublisher, never()).publishCreated(any());
    }

    @Test
    void create_shouldRejectUnknownCategory() {
        Authentication authentication =
                IssueTestFixtures.residentAuthentication();

        when(authServiceClient.getUserProfile(IssueTestFixtures.RESIDENT_ID))
                .thenReturn(validResidentProfile());

        when(categoryRepository.findById(
                IssueTestFixtures.PLUMBING_CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                IssueCategoryNotFoundException.class,
                () -> issueService.create(validCreateRequest(), authentication)
        );

        verify(issueRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(issueEventPublisher, never()).publishCreated(any());
    }

    @Test
    void findById_shouldHideAnotherResidentsIssue() {
        Issue issue = IssueTestFixtures.issue(
                IssueTestFixtures.OTHER_RESIDENT_ID,
                IssueTestFixtures.OTHER_BLOCK_ID,
                "202",
                IssueTestFixtures.plumbingCategory(),
                "Broken sink",
                "The sink is broken.",
                "Bathroom"
        );

        when(issueRepository.findById(IssueTestFixtures.NEW_ISSUE_ID))
                .thenReturn(Optional.of(issue));

        assertThrows(
                IssueNotFoundException.class,
                () -> issueService.findById(
                        IssueTestFixtures.NEW_ISSUE_ID,
                        IssueTestFixtures.residentAuthentication()
                )
        );
    }

    @Test
    void findById_shouldAllowStaffToViewAnotherResidentsIssue() {
        Issue issue = IssueTestFixtures.issue(
                IssueTestFixtures.OTHER_RESIDENT_ID,
                IssueTestFixtures.OTHER_BLOCK_ID,
                "202",
                IssueTestFixtures.plumbingCategory(),
                "Broken sink",
                "The sink is broken.",
                "Bathroom"
        );

        when(issueRepository.findById(IssueTestFixtures.NEW_ISSUE_ID))
                .thenReturn(Optional.of(issue));

        when(historyRepository.findByIssueIdOrderByChangedAtAsc(
                IssueTestFixtures.NEW_ISSUE_ID))
                .thenReturn(List.of());

        IssueDetailsResponse response = issueService.findById(
                IssueTestFixtures.NEW_ISSUE_ID,
                IssueTestFixtures.reviewerAuthentication()
        );

        assertEquals(IssueStatus.NEW, response.status());
        assertEquals(IssueTestFixtures.OTHER_RESIDENT_ID, response.residentId());
        assertTrue(response.statusHistory().isEmpty());
    }

    @Test
    void changeStatus_shouldSaveHistoryAndPublishEvent() {
        Issue issue = IssueTestFixtures.issue();

        when(issueRepository.findById(IssueTestFixtures.NEW_ISSUE_ID))
                .thenReturn(Optional.of(issue));

        when(historyRepository.findByIssueIdOrderByChangedAtAsc(
                IssueTestFixtures.NEW_ISSUE_ID))
                .thenReturn(List.of());

        ChangeIssueStatusRequest request =
                new ChangeIssueStatusRequest(IssueStatus.IN_PROGRESS);

        IssueDetailsResponse response = issueService.changeStatus(
                IssueTestFixtures.NEW_ISSUE_ID,
                request,
                IssueTestFixtures.reviewerAuthentication()
        );

        assertEquals(IssueStatus.IN_PROGRESS, response.status());

        verify(issueRepository).save(issue);
        verify(historyRepository).save(any());
        verify(issueEventPublisher).publishStatusChanged(
                issue, IssueStatus.NEW);
    }

    @Test
    void changeStatus_shouldRejectResident() {
        assertThrows(
                IssueStatusChangeNotAllowedException.class,
                () -> issueService.changeStatus(
                        IssueTestFixtures.NEW_ISSUE_ID,
                        new ChangeIssueStatusRequest(IssueStatus.IN_PROGRESS),
                        IssueTestFixtures.residentAuthentication()
                )
        );

        verify(issueRepository, never()).findById(any());
        verify(issueEventPublisher, never())
                .publishStatusChanged(any(), any());
    }

    @Test
    void changeStatus_shouldRejectInvalidTransition() {
        Issue issue = IssueTestFixtures.issue();
        issue.changeStatus(IssueStatus.CLOSED);

        when(issueRepository.findById(IssueTestFixtures.CLOSED_ISSUE_ID))
                .thenReturn(Optional.of(issue));

        assertThrows(
                InvalidIssueStatusTransitionException.class,
                () -> issueService.changeStatus(
                        IssueTestFixtures.CLOSED_ISSUE_ID,
                        new ChangeIssueStatusRequest(IssueStatus.IN_PROGRESS),
                        IssueTestFixtures.adminAuthentication()
                )
        );

        verify(issueRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
        verify(issueEventPublisher, never())
                .publishStatusChanged(any(), any());
    }

    private static CreateIssueRequest validCreateRequest() {
        return new CreateIssueRequest(
                "Leaking tap",
                "The tap is leaking.",
                IssueTestFixtures.PLUMBING_CATEGORY_ID,
                "Bathroom"
        );
    }

    private static AuthUserProfile validResidentProfile() {
        return new AuthUserProfile(
                IssueTestFixtures.RESIDENT_ID,
                "resident",
                "Test Resident",
                "RESIDENT",
                IssueTestFixtures.BLOCK_ID,
                "101",
                Instant.parse("2026-01-10T10:00:00Z")
        );
    }
}
