// TODO: Test category persistence and the unique category name constraint.

package com.dormsupport.issues.repository;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.category.IssueCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class IssueCategoryRepositoryTest {

    @Autowired
    private IssueCategoryRepository categoryRepository;


    @Test
    void saveAndFindById_shouldPersistCategory() {
        IssueCategory category = new IssueCategory(
                "Test category " + UUID.randomUUID(),
                "Test category description"
        );

        IssueCategory savedCategory = categoryRepository.saveAndFlush(category);

        assertNotNull(savedCategory.getId());

        IssueCategory foundCategory = categoryRepository
                .findById(savedCategory.getId())
                .orElseThrow();

        assertEquals(savedCategory.getId(), foundCategory.getId());
        assertEquals(savedCategory.getName(), foundCategory.getName());
        assertEquals(
                "Test category description",
                foundCategory.getDescription()
        );
    }

    @Test
    void saveAndFlush_shouldRejectDuplicateCategoryName() {
        String categoryName = "Duplicate category " + UUID.randomUUID();

        categoryRepository.saveAndFlush(
                new IssueCategory(categoryName, "First category")
        );

        categoryRepository.save(
                new IssueCategory(categoryName, "Second category")
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> categoryRepository.flush()
        );
    }
}
