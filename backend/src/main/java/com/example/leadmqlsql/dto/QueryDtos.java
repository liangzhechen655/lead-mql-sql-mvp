package com.example.leadmqlsql.dto;

import jakarta.validation.constraints.NotBlank;

public final class QueryDtos {
    private QueryDtos() {
    }

    public record NaturalLanguageQueryRequest(@NotBlank String question) {
    }

    public record NaturalLanguageQueryResponse(String question, String answer, String matchedRule) {
    }
}
