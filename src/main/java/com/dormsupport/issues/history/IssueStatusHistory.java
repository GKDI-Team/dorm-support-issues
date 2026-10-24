package com.dormsupport.issues.history;

import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "issue_status_history")
public class IssueStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_issue_status_history_issue"))
    private Issue issue;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private IssueStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private IssueStatus toStatus;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected IssueStatusHistory() {
        // Required by JPA.
    }

    public IssueStatusHistory(Issue issue, IssueStatus fromStatus,
                              IssueStatus toStatus, UUID changedBy) {
        this.issue = issue;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
    }

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Issue getIssue() {
        return issue;
    }

    public IssueStatus getFromStatus() {
        return fromStatus;
    }

    public IssueStatus getToStatus() {
        return toStatus;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
