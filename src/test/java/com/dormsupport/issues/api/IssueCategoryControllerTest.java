package com.dormsupport.issues.api;

import com.dormsupport.issues.category.IssueCategoryController;
import com.dormsupport.issues.category.IssueCategoryResponse;
import com.dormsupport.issues.category.IssueCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dormsupport.issues.security.JwtService;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;


import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IssueCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class IssueCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueCategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void findAll_shouldReturnCategories() throws Exception {
        UUID id = UUID.randomUUID();

        when(categoryService.findAll()).thenReturn(List.of(
                new IssueCategoryResponse(
                        id,
                        "Plumbing",
                        "Water supply and plumbing issues"
                )
        ));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Plumbing"))
                .andExpect(jsonPath("$[0].description")
                        .value("Water supply and plumbing issues"));

        verify(categoryService).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyArrayWhenNoCategoriesExist() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(categoryService).findAll();
    }
}