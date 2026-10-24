package com.dormsupport.issues.issue;

import com.dormsupport.issues.category.IssueCategory;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "block_id", nullable = false)
    private UUID blockId;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @jakarta.persistence.ForeignKey(name = "fk_issues_category"))
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueStatus status;

    @Column(name = "location_details", nullable = false, length = 250)
    private String locationDetails;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Issue() {
        // Required by JPA.
    }

    public Issue(UUID residentId, UUID blockId, String roomNumber, String title,
                 String description, IssueCategory category, String locationDetails) {
        this.residentId = residentId;
        this.blockId = blockId;
        this.roomNumber = roomNumber;
        this.title = title;
        this.description = description;
        this.category = category;
        this.locationDetails = locationDetails;
        this.status = IssueStatus.NEW;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = IssueStatus.NEW;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getResidentId() {
        return residentId;
    }

    public UUID getBlockId() {
        return blockId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public String getLocationDetails() {
        return locationDetails;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void changeStatus(IssueStatus status) {
        this.status = status;
    }
}
