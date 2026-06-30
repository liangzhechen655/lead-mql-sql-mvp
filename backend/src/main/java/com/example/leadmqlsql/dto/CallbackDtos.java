package com.example.leadmqlsql.dto;

import jakarta.validation.constraints.NotNull;

public final class CallbackDtos {
    private CallbackDtos() {
    }

    public record CallResultRequest(
            @NotNull Long leadId,
            boolean connected,
            boolean valid,
            String invalidReason,
            String rawResult
    ) {
    }

    public record WechatResultRequest(
            @NotNull Long leadId,
            boolean added,
            String failedReason,
            String externalUserId
    ) {
    }
}
