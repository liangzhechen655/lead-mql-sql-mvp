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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_record")
public class SalesLead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String customerName;

    @Column(nullable = false, unique = true, length = 30)
    private String phone;

    @Column(nullable = false, length = 80)
    private String source;

    @Column(length = 80)
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CallStatus callStatus = CallStatus.NOT_CALLED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WechatStatus wechatStatus = WechatStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadGrade grade = LeadGrade.UNKNOWN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private SalesUser owner;

    @Column(length = 200)
    private String invalidReason;

    @Column(length = 500)
    private String remark;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime importedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime lastFollowUpAt;
    private LocalDateTime mqlAt;
    private LocalDateTime sqlAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    public CallStatus getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(CallStatus callStatus) {
        this.callStatus = callStatus;
    }

    public WechatStatus getWechatStatus() {
        return wechatStatus;
    }

    public void setWechatStatus(WechatStatus wechatStatus) {
        this.wechatStatus = wechatStatus;
    }

    public LeadGrade getGrade() {
        return grade;
    }

    public void setGrade(LeadGrade grade) {
        this.grade = grade;
    }

    public SalesUser getOwner() {
        return owner;
    }

    public void setOwner(SalesUser owner) {
        this.owner = owner;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getLastFollowUpAt() {
        return lastFollowUpAt;
    }

    public void setLastFollowUpAt(LocalDateTime lastFollowUpAt) {
        this.lastFollowUpAt = lastFollowUpAt;
    }

    public LocalDateTime getMqlAt() {
        return mqlAt;
    }

    public void setMqlAt(LocalDateTime mqlAt) {
        this.mqlAt = mqlAt;
    }

    public LocalDateTime getSqlAt() {
        return sqlAt;
    }

    public void setSqlAt(LocalDateTime sqlAt) {
        this.sqlAt = sqlAt;
    }
}
