package com.dormsupport.issues.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueCategoryRepository extends JpaRepository<IssueCategory, UUID> {

    List<IssueCategory> findAllByOrderByNameAsc();
}
