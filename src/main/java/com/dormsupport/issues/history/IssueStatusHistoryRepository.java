package com.dormsupport.issues.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, UUID> {

    List<IssueStatusHistory> findByIssueIdOrderByChangedAtAsc(UUID issueId);
}
