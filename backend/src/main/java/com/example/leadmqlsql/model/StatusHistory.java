package com.example.leadmqlsql.model;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
public class StatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private SalesLead lead;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private LeadStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LeadStatus toStatus;

    @Column(nullable = false, length = 80)
    private String operatorName;

    @Column(length = 500)
    private String reason;

    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SalesLead getLead() {
        return lead;
    }

    public void setLead(SalesLead lead) {
        this.lead = lead;
    }

    public LeadStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(LeadStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public LeadStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(LeadStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
