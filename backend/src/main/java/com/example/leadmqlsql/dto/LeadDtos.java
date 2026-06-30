package com.example.leadmqlsql.dto;

import com.example.leadmqlsql.model.CallStatus;
import com.example.leadmqlsql.model.LeadGrade;
import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.WechatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class LeadDtos {
    private LeadDtos() {
    }

    public record LeadCreateRequest(
            @NotBlank String customerName,
            @NotBlank String phone,
            @NotBlank String source,
            String channel,
            LeadGrade grade,
            String remark
    ) {
    }

    public record StatusUpdateRequest(
            @NotNull LeadStatus status,
            String reason,
            String operatorName,
            String invalidReason
    ) {
    }

    public record AssignRequest(
            @NotNull Long ownerId,
            String operatorName
    ) {
    }

    public record FollowUpRequest(
            Long operatorId,
            @NotBlank String method,
            @NotBlank String content,
            LocalDateTime nextFollowUpAt
    ) {
    }

    public record LeadResponse(
            Long id,
            String customerName,
            String phone,
            String source,
            String channel,
            LeadStatus status,
            String statusLabel,
            CallStatus callStatus,
            WechatStatus wechatStatus,
            LeadGrade grade,
            Long ownerId,
            String ownerName,
            String invalidReason,
            String remark,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime assignedAt,
            LocalDateTime lastFollowUpAt,
            LocalDateTime mqlAt,
            LocalDateTime sqlAt
    ) {
    }

    public record LeadDetailResponse(
            LeadResponse lead,
            List<FollowUpResponse> followUps,
            List<StatusHistoryResponse> statusHistory,
            List<OperationLogResponse> operationLogs
    ) {
    }

    public record FollowUpResponse(
            Long id,
            String operatorName,
            String method,
            String content,
            LocalDateTime nextFollowUpAt,
            LocalDateTime createdAt
    ) {
    }

    public record StatusHistoryResponse(
            Long id,
            LeadStatus fromStatus,
            LeadStatus toStatus,
            String operatorName,
            String reason,
            LocalDateTime changedAt
    ) {
    }

    public record OperationLogResponse(
            Long id,
            String action,
            String operatorName,
            String detail,
            LocalDateTime createdAt
    ) {
    }

    public record ImportResult(
            Long batchId,
            int totalRows,
            int insertedRows,
            int duplicateRows,
            int failedRows
    ) {
    }

    public record SalesUserResponse(Long id, String name, String role, String team) {
    }
}
