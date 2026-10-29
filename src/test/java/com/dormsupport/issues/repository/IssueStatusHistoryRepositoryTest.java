package com.dormsupport.issues.repository;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.category.IssueCategoryRepository;
import com.dormsupport.issues.history.IssueStatusHistory;
import com.dormsupport.issues.history.IssueStatusHistoryRepository;
import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueRepository;
import com.dormsupport.issues.issue.IssueStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class IssueStatusHistoryRepositoryTest {

    @Autowired
    private IssueStatusHistoryRepository historyRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueCategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Issue firstIssue;
    private Issue secondIssue;
    private UUID changedBy;

    @BeforeEach
    void setUp() {
        IssueCategory category = categoryRepository.saveAndFlush(
                new IssueCategory(
                        "History test " + UUID.randomUUID(),
                        "Category for status history tests"
                )
        );

        firstIssue = issueRepository.saveAndFlush(createIssue(category, "First issue"));
        secondIssue = issueRepository.saveAndFlush(createIssue(category, "Second issue"));

        changedBy = UUID.randomUUID();
    }

    @Test
    void saveAndFindById_shouldPersistStatusHistory() {
        IssueStatusHistory history = new IssueStatusHistory(
                firstIssue,
                IssueStatus.NEW,
                IssueStatus.IN_PROGRESS,
                changedBy
        );

        IssueStatusHistory savedHistory = historyRepository.saveAndFlush(history);

        assertNotNull(savedHistory.getId());
        assertNotNull(savedHistory.getChangedAt());
        assertEquals(firstIssue.getId(), savedHistory.getIssue().getId());
        assertEquals(IssueStatus.NEW, savedHistory.getFromStatus());
        assertEquals(IssueStatus.IN_PROGRESS, savedHistory.getToStatus());
        assertEquals(changedBy, savedHistory.getChangedBy());

        entityManager.clear();

        IssueStatusHistory foundHistory = historyRepository
                .findById(savedHistory.getId())
                .orElseThrow();

        assertEquals(savedHistory.getId(), foundHistory.getId());
        assertEquals(firstIssue.getId(), foundHistory.getIssue().getId());
        assertEquals(IssueStatus.NEW, foundHistory.getFromStatus());
        assertEquals(IssueStatus.IN_PROGRESS, foundHistory.getToStatus());
        assertEquals(changedBy, foundHistory.getChangedBy());
    }

    @Test
    void findByIssueIdOrderByChangedAtAsc_shouldReturnHistoryChronologicallyForOnlyRequestedIssue() {
        IssueStatusHistory latestHistory = historyRepository.saveAndFlush(
                new IssueStatusHistory(
                        firstIssue,
                        IssueStatus.IN_PROGRESS,
                        IssueStatus.CLOSED,
                        changedBy
                )
        );

        IssueStatusHistory earliestHistory = historyRepository.saveAndFlush(
                new IssueStatusHistory(
                        firstIssue,
                        null,
                        IssueStatus.NEW,
                        changedBy
                )
        );

        historyRepository.saveAndFlush(
                new IssueStatusHistory(
                        secondIssue,
                        IssueStatus.NEW,
                        IssueStatus.IN_PROGRESS,
                        changedBy
                )
        );

        Instant earliestTime = Instant.parse("2026-01-10T10:00:00Z");
        Instant latestTime = Instant.parse("2026-01-12T10:00:00Z");

        setChangedAt(earliestHistory.getId(), earliestTime);
        setChangedAt(latestHistory.getId(), latestTime);

        List<IssueStatusHistory> history =
                historyRepository.findByIssueIdOrderByChangedAtAsc(firstIssue.getId());

        assertEquals(2, history.size());

        assertEquals(earliestHistory.getId(), history.get(0).getId());
        assertEquals(earliestTime, history.get(0).getChangedAt());
        assertEquals(IssueStatus.NEW, history.get(0).getToStatus());

        assertEquals(latestHistory.getId(), history.get(1).getId());
        assertEquals(latestTime, history.get(1).getChangedAt());
        assertEquals(IssueStatus.CLOSED, history.get(1).getToStatus());

        assertEquals(
                firstIssue.getId(),
                history.get(0).getIssue().getId()
        );
        assertEquals(
                firstIssue.getId(),
                history.get(1).getIssue().getId()
        );
    }

    private Issue createIssue(IssueCategory category, String title) {
        return new Issue(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "101",
                title,
                "Test issue description",
                category,
                "Near the entrance"
        );
    }

    private void setChangedAt(UUID historyId, Instant changedAt) {
        entityManager.createNativeQuery(
                        "UPDATE issue_status_history SET changed_at = :changedAt WHERE id = :id"
                )
                .setParameter("changedAt", changedAt)
                .setParameter("id", historyId)
                .executeUpdate();

        entityManager.clear();
    }
}