package com.dormsupport.issues.issue;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/issues")
@Validated
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> create(@Valid @RequestBody CreateIssueRequest request,
                                                Authentication authentication) {
        IssueResponse response = issueService.create(request, authentication);
        return ResponseEntity.created(URI.create("/api/v1/issues/" + response.id())).body(response);
    }

    @GetMapping
    public List<IssueResponse> findAll(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100_000) int offset,
            Authentication authentication) {
        return issueService.findAll(authentication, status, limit, offset);
    }

    @GetMapping("/{issueId}")
    public IssueDetailsResponse findById(@PathVariable UUID issueId, Authentication authentication) {
        return issueService.findById(issueId, authentication);
    }

    @PatchMapping("/{issueId}/status")
    public IssueDetailsResponse changeStatus(@PathVariable UUID issueId,
                                             @Valid @RequestBody ChangeIssueStatusRequest request,
                                             Authentication authentication) {
        return issueService.changeStatus(issueId, request, authentication);
    }
}
