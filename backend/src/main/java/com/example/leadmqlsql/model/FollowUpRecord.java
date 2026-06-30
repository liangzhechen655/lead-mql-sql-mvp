package com.example.leadmqlsql.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "follow_up_record")
public class FollowUpRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private SalesLead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private SalesUser operator;

    @Column(nullable = false, length = 50)
    private String method;

    @Column(nullable = false, length = 1000)
    private String content;

    private LocalDateTime nextFollowUpAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
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

    public SalesUser getOperator() {
        return operator;
    }

    public void setOperator(SalesUser operator) {
        this.operator = operator;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getNextFollowUpAt() {
        return nextFollowUpAt;
    }

    public void setNextFollowUpAt(LocalDateTime nextFollowUpAt) {
        this.nextFollowUpAt = nextFollowUpAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
