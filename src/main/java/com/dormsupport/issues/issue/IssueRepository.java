package com.dormsupport.issues.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    List<Issue> findByResidentIdOrderByCreatedAtDesc(UUID residentId);

    List<Issue> findByStatusOrderByCreatedAtDesc(IssueStatus status);

    List<Issue> findAllByOrderByCreatedAtDesc();
}
