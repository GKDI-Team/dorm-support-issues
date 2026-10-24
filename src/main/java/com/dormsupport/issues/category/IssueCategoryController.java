package com.dormsupport.issues.category;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class IssueCategoryController {

    private final IssueCategoryService categoryService;

    public IssueCategoryController(IssueCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<IssueCategoryResponse> findAll() {
        return categoryService.findAll();
    }
}
