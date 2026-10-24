package com.dormsupport.issues.category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IssueCategoryService {

    private final IssueCategoryRepository categoryRepository;

    public IssueCategoryService(IssueCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<IssueCategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(IssueCategoryResponse::from)
                .toList();
    }
}
