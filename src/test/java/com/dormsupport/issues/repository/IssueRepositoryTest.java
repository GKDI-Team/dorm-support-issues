package com.dormsupport.issues.repository;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.category.IssueCategoryRepository;
import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueRepository;
import com.dormsupport.issues.issue.IssueStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class IssueRepositoryTest {

@Autowired
private IssueRepository issueRepository;

@Autowired
private IssueCategoryRepository categoryRepository;

@Autowired
private EntityManager entityManager;

private UUID residentId;
private UUID anotherResidentId;
private UUID blockId;
private IssueCategory category;

@BeforeEach
void setUp() {
    residentId = UUID.randomUUID();
    anotherResidentId = UUID.randomUUID();
    blockId = UUID.randomUUID();

    category = categoryRepository.saveAndFlush(
            new IssueCategory(
                    "Test category " + UUID.randomUUID(),
                    "Test category description"
            )
    );
}

@Test
void saveAndFindById_shouldPersistIssue() {
    Issue issue = createIssue(residentId, "Leaking tap");

    Issue savedIssue = issueRepository.saveAndFlush(issue);

    assertNotNull(savedIssue.getId());

    Issue foundIssue = issueRepository.findById(savedIssue.getId())
            .orElseThrow();

    assertEquals(savedIssue.getId(), foundIssue.getId());
    assertEquals(residentId, foundIssue.getResidentId());
    assertEquals("Leaking tap", foundIssue.getTitle());
    assertEquals(IssueStatus.NEW, foundIssue.getStatus());
    assertNotNull(foundIssue.getCreatedAt());
    assertNotNull(foundIssue.getUpdatedAt());
}

@Test
void findByResidentIdOrderByCreatedAtDesc_shouldReturnOnlyResidentsIssuesNewestFirst() {
    Issue olderIssue = issueRepository.saveAndFlush(
            createIssue(residentId, "Older issue")
    );
    Issue newerIssue = issueRepository.saveAndFlush(
            createIssue(residentId, "Newer issue")
    );
    issueRepository.saveAndFlush(
            createIssue(anotherResidentId, "Another resident's issue")
    );

    setCreatedAt(olderIssue.getId(), Instant.parse("2026-01-01T10:00:00Z"));
    setCreatedAt(newerIssue.getId(), Instant.parse("2026-01-02T10:00:00Z"));

    List<Issue> issues =
            issueRepository.findByResidentIdOrderByCreatedAtDesc(residentId);

    assertEquals(2, issues.size());
    assertEquals(newerIssue.getId(), issues.get(0).getId());
    assertEquals(olderIssue.getId(), issues.get(1).getId());
    assertTrue(issues.stream().allMatch(
            issue -> issue.getResidentId().equals(residentId)
    ));
}

@Test
void findByStatusOrderByCreatedAtDesc_shouldReturnOnlyMatchingStatus() {
    Issue newIssue = issueRepository.saveAndFlush(
            createIssue(residentId, "New issue")
    );
    Issue inProgressIssue = issueRepository.saveAndFlush(
            createIssue(residentId, "Issue in progress")
    );
    inProgressIssue.changeStatus(IssueStatus.IN_PROGRESS);
    issueRepository.saveAndFlush(inProgressIssue);

    setCreatedAt(newIssue.getId(), Instant.parse("2026-01-01T10:00:00Z"));
    setCreatedAt(inProgressIssue.getId(), Instant.parse("2026-01-02T10:00:00Z"));

    List<Issue> issues = issueRepository
        .findByStatusOrderByCreatedAtDesc(IssueStatus.NEW)
        .stream()
        .filter(issue -> issue.getResidentId().equals(residentId))
        .toList();

    assertEquals(1, issues.size());
    assertEquals(newIssue.getId(), issues.get(0).getId());
    assertTrue(issues.stream().allMatch(
            issue -> issue.getStatus() == IssueStatus.NEW
    ));
}

@Test
void findAllByOrderByCreatedAtDesc_shouldReturnNewestIssuesFirst() {
    Issue olderIssue = issueRepository.saveAndFlush(
            createIssue(residentId, "Older issue")
    );
    Issue newerIssue = issueRepository.saveAndFlush(
            createIssue(anotherResidentId, "Newer issue")
    );

    setCreatedAt(olderIssue.getId(), Instant.parse("2026-01-01T10:00:00Z"));
    setCreatedAt(newerIssue.getId(), Instant.parse("2026-01-02T10:00:00Z"));

    List<Issue> issues = issueRepository.findAllByOrderByCreatedAtDesc();

    assertTrue(issues.size() >= 2);

    int olderIndex = indexOf(issues, olderIssue.getId());
    int newerIndex = indexOf(issues, newerIssue.getId());

    assertTrue(newerIndex < olderIndex);
}

@Test
void findByResidentId_shouldReturnOnlyResidentsIssuesInRequestedPage() {
    issueRepository.saveAndFlush(createIssue(residentId, "First issue"));
    issueRepository.saveAndFlush(createIssue(residentId, "Second issue"));
    issueRepository.saveAndFlush(createIssue(residentId, "Third issue"));
    issueRepository.saveAndFlush(createIssue(anotherResidentId, "Other resident issue"));

    Page<Issue> page = issueRepository.findByResidentId(
            residentId,
            PageRequest.of(0, 2, Sort.by("title").ascending())
    );

    assertEquals(3, page.getTotalElements());
    assertEquals(2, page.getContent().size());
    assertTrue(page.getContent().stream().allMatch(
            issue -> issue.getResidentId().equals(residentId)
    ));
}

@Test
void findByResidentIdAndStatus_shouldFilterByResidentAndStatus() {
    issueRepository.saveAndFlush(
            createIssue(residentId, "New issue")
    );
    Issue inProgressIssue = createIssue(residentId, "In progress issue");
    inProgressIssue.changeStatus(IssueStatus.IN_PROGRESS);
    issueRepository.saveAndFlush(inProgressIssue);

    Issue anotherResidentsIssue = createIssue(
            anotherResidentId,
            "Another resident's issue"
    );
    anotherResidentsIssue.changeStatus(IssueStatus.IN_PROGRESS);
    issueRepository.saveAndFlush(anotherResidentsIssue);

    Page<Issue> page = issueRepository.findByResidentIdAndStatus(
            residentId,
            IssueStatus.IN_PROGRESS,
            PageRequest.of(0, 10)
    );

    assertEquals(1, page.getTotalElements());
    assertEquals(inProgressIssue.getId(), page.getContent().get(0).getId());
    assertTrue(page.getContent().stream().allMatch(
            issue -> issue.getResidentId().equals(residentId)
                    && issue.getStatus() == IssueStatus.IN_PROGRESS
    ));
}

@Test
void findByStatus_shouldReturnOnlyMatchingStatusInPage() {
    issueRepository.saveAndFlush(
            createIssue(residentId, "New issue")
    );

    Issue closedIssue = createIssue(residentId, "Closed issue");
    closedIssue.changeStatus(IssueStatus.CLOSED);
    issueRepository.saveAndFlush(closedIssue);

    Page<Issue> page = issueRepository.findByStatus(
        IssueStatus.CLOSED,
        PageRequest.of(0, 100)
    );

    List<Issue> residentIssues = page.getContent()
        .stream()
        .filter(issue -> issue.getResidentId().equals(residentId))
        .toList();

    assertEquals(1, residentIssues.size());
    assertEquals(closedIssue.getId(), residentIssues.get(0).getId());
    assertTrue(residentIssues.stream().allMatch(
            issue -> issue.getStatus() == IssueStatus.CLOSED
    ));
}

private Issue createIssue(UUID issueResidentId, String title) {
    return new Issue(
            issueResidentId,
            blockId,
            "101",
            title,
            "Test issue description",
            category,
            "Near the entrance"
    );
}

private void setCreatedAt(UUID issueId, Instant createdAt) {
    entityManager.createNativeQuery(
                    "UPDATE issues SET created_at = :createdAt WHERE id = :id"
            )
            .setParameter("createdAt", createdAt)
            .setParameter("id", issueId)
            .executeUpdate();

    entityManager.clear();
}

private int indexOf(List<Issue> issues, UUID issueId) {
    for (int i = 0; i < issues.size(); i++) {
        if (issues.get(i).getId().equals(issueId)) {
            return i;
        }
    }

    throw new AssertionError("Issue not found: " + issueId);
}

}