package com.dormsupport.issues.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    List<Issue> findByResidentIdOrderByCreatedAtDesc(UUID residentId);

    List<Issue> findByStatusOrderByCreatedAtDesc(IssueStatus status);

    List<Issue> findAllByOrderByCreatedAtDesc();

    Page<Issue> findByResidentId(UUID residentId, Pageable pageable);

    Page<Issue> findByResidentIdAndStatus(UUID residentId, IssueStatus status, Pageable pageable);

    Page<Issue> findByStatus(IssueStatus status, Pageable pageable);
}
