package com.dormsupport.issues.api;

import com.dormsupport.issues.issue.ChangeIssueStatusRequest;
import com.dormsupport.issues.issue.CreateIssueRequest;
import com.dormsupport.issues.issue.IssueController;
import com.dormsupport.issues.issue.IssueDetailsResponse;
import com.dormsupport.issues.issue.IssueResponse;
import com.dormsupport.issues.issue.IssueService;
import com.dormsupport.issues.issue.IssueStatus;
import com.dormsupport.issues.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IssueController.class)
@AutoConfigureMockMvc(addFilters = false)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueService issueService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void create_shouldReturnCreatedAndLocation() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        IssueResponse response = new IssueResponse(
                issueId, residentId, blockId, "101",
                "Leaking tap", "The tap is leaking",
                categoryId, "Plumbing", IssueStatus.NEW,
                "Bathroom", Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"));

        when(issueService.create(any(CreateIssueRequest.class), nullable(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Leaking tap",
                                  "description": "The tap is leaking",
                                  "categoryId": "%s",
                                  "locationDetails": "Bathroom"
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/issues/" + issueId))
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.title").value("Leaking tap"))
                .andExpect(jsonPath("$.status").value("NEW"));

        verify(issueService).create(any(CreateIssueRequest.class), nullable(Authentication.class));
    }

    @Test
    void create_shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "The tap is leaking",
                                  "categoryId": "%s",
                                  "locationDetails": "Bathroom"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequestWhenCategoryIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Leaking tap",
                                  "description": "The tap is leaking",
                                  "locationDetails": "Bathroom"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAll_shouldReturnIssuesAndPassQueryParameters() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        IssueResponse response = new IssueResponse(
                issueId, residentId, blockId, "101",
                "Leaking tap", "The tap is leaking",
                categoryId, "Plumbing", IssueStatus.NEW,
                "Bathroom", Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"));

        when(issueService.findAll(nullable(Authentication.class), eq(IssueStatus.NEW), eq(10), eq(5)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/issues")
                        .param("status", "NEW")
                        .param("limit", "10")
                        .param("offset", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(issueId.toString()))
                .andExpect(jsonPath("$[0].title").value("Leaking tap"))
                .andExpect(jsonPath("$[0].status").value("NEW"));

        verify(issueService).findAll(nullable(Authentication.class), eq(IssueStatus.NEW), eq(10), eq(5));
    }

    @Test
    void findAll_shouldUseDefaultPagination() throws Exception {
        when(issueService.findAll(nullable(Authentication.class), eq(null), eq(20), eq(0)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/issues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(issueService).findAll(nullable(Authentication.class), eq(null), eq(20), eq(0));
    }

    @Test
    void findAll_shouldRejectLimitOutsideAllowedRange() throws Exception {
        mockMvc.perform(get("/api/v1/issues").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturnIssueDetails() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        IssueDetailsResponse response = new IssueDetailsResponse(
                issueId, residentId, blockId, "101",
                "Leaking tap", "The tap is leaking",
                categoryId, "Plumbing", IssueStatus.NEW,
                "Bathroom", Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"),
                List.of());

        when(issueService.findById(eq(issueId), nullable(Authentication.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/issues/{issueId}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.title").value("Leaking tap"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.statusHistory").isArray())
                .andExpect(jsonPath("$.statusHistory").isEmpty());

        verify(issueService).findById(eq(issueId), nullable(Authentication.class));
    }

    @Test
    void changeStatus_shouldReturnUpdatedIssueDetails() throws Exception {
        UUID issueId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        IssueDetailsResponse response = new IssueDetailsResponse(
                issueId, residentId, blockId, "101",
                "Leaking tap", "The tap is leaking",
                categoryId, "Plumbing", IssueStatus.IN_PROGRESS,
                "Bathroom", Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T11:00:00Z"),
                List.of());

        when(issueService.changeStatus(eq(issueId), any(ChangeIssueStatusRequest.class),
                nullable(Authentication.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/issues/{issueId}/status", issueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(issueService).changeStatus(eq(issueId),
                any(ChangeIssueStatusRequest.class), nullable(Authentication.class));
    }

    @Test
    void changeStatus_shouldReturnBadRequestWhenStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/api/v1/issues/{issueId}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}