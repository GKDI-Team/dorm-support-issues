package com.dormsupport.issues.integration;

import com.dormsupport.issues.client.AuthServiceClient;
import com.dormsupport.issues.event.IssueEventPublisher;
import com.dormsupport.issues.history.IssueStatusHistoryRepository;
import com.dormsupport.issues.issue.IssueRepository;
import com.dormsupport.issues.issue.IssueStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueFlowIntegrationTest {

    private static final String TEST_SECRET =
            "issues-service-test-only-secret-do-not-use-in-production";

    private static final UUID CATEGORY_ID =
            UUID.fromString("11111111-1111-4111-8111-111111111111");

    private static final UUID BLOCK_ID =
            UUID.fromString("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb");

    private static final String RESIDENT_PROFILE_JSON = """
            {
              "id": "%s",
              "login": "resident-test",
              "fullName": "Test Resident",
              "role": "RESIDENT",
              "blockId": "%s",
              "roomNumber": "101",
              "createdAt": "2026-01-01T10:00:00Z"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules();

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueStatusHistoryRepository historyRepository;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private IssueEventPublisher issueEventPublisher;

    @Test
    void residentCreatesIssueAndStaffChangesItsStatus() throws Exception {
        UUID residentId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();

        when(authServiceClient.getUserProfile(residentId))
                .thenReturn(objectMapper.readValue(
                        RESIDENT_PROFILE_JSON.formatted(residentId, BLOCK_ID),
                        com.dormsupport.issues.client.AuthUserProfile.class));

        String createRequest = """
                {
                  "title": "  Leaking tap  ",
                  "description": "  The tap is leaking in the bathroom.  ",
                  "categoryId": "%s",
                  "locationDetails": "  Bathroom  "
                }
                """.formatted(CATEGORY_ID);

        MvcResult createResult = mockMvc.perform(post("/api/v1/issues")
                        .header("Authorization", bearerToken(residentId, "RESIDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.residentId").value(residentId.toString()))
                .andExpect(jsonPath("$.blockId").value(BLOCK_ID.toString()))
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.title").value("Leaking tap"))
                .andExpect(jsonPath("$.description").value(
                        "The tap is leaking in the bathroom."))
                .andExpect(jsonPath("$.categoryId").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.locationDetails").value("Bathroom"))
                .andReturn();

        JsonNode createdIssue = objectMapper.readTree(
                createResult.getResponse().getContentAsString());
        UUID issueId = UUID.fromString(createdIssue.get("id").asText());

        assertEquals(true, issueRepository.existsById(issueId));
        assertEquals(IssueStatus.NEW, issueRepository.findById(issueId)
                .orElseThrow().getStatus());

        String changeStatusRequest = """
                {
                  "status": "IN_PROGRESS"
                }
                """;

        mockMvc.perform(patch("/api/v1/issues/{issueId}/status", issueId)
                        .header("Authorization", bearerToken(staffId, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeStatusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.statusHistory.length()").value(2))
                .andExpect(jsonPath("$.statusHistory[0].toStatus").value("NEW"))
                .andExpect(jsonPath("$.statusHistory[1].fromStatus").value("NEW"))
                .andExpect(jsonPath("$.statusHistory[1].toStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.statusHistory[1].changedBy").value(staffId.toString()));

        assertEquals(IssueStatus.IN_PROGRESS, issueRepository.findById(issueId)
                .orElseThrow().getStatus());

        var history = historyRepository.findByIssueIdOrderByChangedAtAsc(issueId);
        assertEquals(2, history.size());

        assertEquals(null, history.get(0).getFromStatus());
        assertEquals(IssueStatus.NEW, history.get(0).getToStatus());
        assertEquals(residentId, history.get(0).getChangedBy());

        assertEquals(IssueStatus.NEW, history.get(1).getFromStatus());
        assertEquals(IssueStatus.IN_PROGRESS, history.get(1).getToStatus());
        assertEquals(staffId, history.get(1).getChangedBy());
    }

    private static String bearerToken(UUID userId, String role) throws Exception {
        String header = base64Url("""
                {"alg":"HS256","typ":"JWT"}
                """.trim());

        String payload = base64Url("""
                {"sub":"%s","role":"%s","exp":%d}
                """.formatted(userId, role, Instant.now().plusSeconds(600).getEpochSecond()).trim());

        String content = header + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));

        return "Bearer " + content + "." + signature;
    }

    private static String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}