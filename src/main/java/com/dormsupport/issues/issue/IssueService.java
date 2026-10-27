package com.dormsupport.issues.issue;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.category.IssueCategoryRepository;
import com.dormsupport.issues.client.AuthServiceClient;
import com.dormsupport.issues.client.AuthUserProfile;
import com.dormsupport.issues.event.IssueEventPublisher;
import com.dormsupport.issues.history.IssueStatusHistory;
import com.dormsupport.issues.history.IssueStatusHistoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final IssueCategoryRepository categoryRepository;
    private final IssueStatusHistoryRepository historyRepository;
    private final AuthServiceClient authServiceClient;
    private final IssueEventPublisher issueEventPublisher;

    public IssueService(IssueRepository issueRepository,
                        IssueCategoryRepository categoryRepository,
                        IssueStatusHistoryRepository historyRepository,
                        AuthServiceClient authServiceClient,
                        IssueEventPublisher issueEventPublisher) {
        this.issueRepository = issueRepository;
        this.categoryRepository = categoryRepository;
        this.historyRepository = historyRepository;
        this.authServiceClient = authServiceClient;
        this.issueEventPublisher = issueEventPublisher;
    }

    @Transactional
    public IssueResponse create(CreateIssueRequest request, Authentication authentication) {
        UUID userId = userId(authentication);
        requireRole(authentication, "RESIDENT");

        AuthUserProfile profile = authServiceClient.getUserProfile(userId);
        if (profile.blockId() == null || profile.roomNumber() == null || profile.roomNumber().isBlank()) {
            throw new ResidentProfileIncompleteException();
        }

        IssueCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(IssueCategoryNotFoundException::new);
        Issue issue = issueRepository.save(new Issue(
                userId,
                profile.blockId(),
                profile.roomNumber(),
                request.title().trim(),
                request.description().trim(),
                category,
                request.locationDetails().trim()));
        historyRepository.save(new IssueStatusHistory(issue, null, IssueStatus.NEW, userId));
        issueEventPublisher.publishCreated(issue);
        return IssueResponse.from(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> findAll(Authentication authentication, IssueStatus status,
                                       int limit, int offset) {
        UUID userId = userId(authentication);
        PageRequest pageRequest = PageRequest.of(
                0,
                Math.addExact(offset, limit),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));

        List<Issue> issues;
        if (isStaff(authentication)) {
            issues = (status == null
                    ? issueRepository.findAll(pageRequest)
                    : issueRepository.findByStatus(status, pageRequest)).getContent();
        } else {
            issues = (status == null
                    ? issueRepository.findByResidentId(userId, pageRequest)
                    : issueRepository.findByResidentIdAndStatus(userId, status, pageRequest)).getContent();
        }

        issues = issues.stream().skip(offset).limit(limit).toList();
        return issues.stream().map(IssueResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public IssueDetailsResponse findById(UUID issueId, Authentication authentication) {
        Issue issue = getVisibleIssue(issueId, authentication);
        return IssueDetailsResponse.from(issue, historyRepository.findByIssueIdOrderByChangedAtAsc(issueId));
    }

    @Transactional
    public IssueDetailsResponse changeStatus(UUID issueId, ChangeIssueStatusRequest request,
                                             Authentication authentication) {
        requireStaff(authentication);
        Issue issue = issueRepository.findById(issueId).orElseThrow(IssueNotFoundException::new);
        if (!issue.getStatus().canTransitionTo(request.status())) {
            throw new InvalidIssueStatusTransitionException();
        }

        IssueStatus previousStatus = issue.getStatus();
        issue.changeStatus(request.status());
        issueRepository.save(issue);
        historyRepository.save(new IssueStatusHistory(
                issue, previousStatus, request.status(), userId(authentication)));
        issueEventPublisher.publishStatusChanged(issue, previousStatus);
        return IssueDetailsResponse.from(issue, historyRepository.findByIssueIdOrderByChangedAtAsc(issueId));
    }

    private Issue getVisibleIssue(UUID issueId, Authentication authentication) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(IssueNotFoundException::new);
        if (!isStaff(authentication) && !issue.getResidentId().equals(userId(authentication))) {
            throw new IssueNotFoundException();
        }
        return issue;
    }

    private static UUID userId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    private static boolean isStaff(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_REVIEWER"));
    }

    private static void requireStaff(Authentication authentication) {
        if (!isStaff(authentication)) {
            throw new IssueStatusChangeNotAllowedException();
        }
    }

    private static void requireRole(Authentication authentication, String role) {
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
        if (!hasRole) {
            throw new IssueStatusChangeNotAllowedException();
        }
    }
}
