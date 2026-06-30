package com.example.leadmqlsql.controller;

import com.example.leadmqlsql.dto.QueryDtos.NaturalLanguageQueryRequest;
import com.example.leadmqlsql.dto.QueryDtos.NaturalLanguageQueryResponse;
import com.example.leadmqlsql.service.QueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
public class QueryController {
    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/natural-language")
    public NaturalLanguageQueryResponse naturalLanguage(@Valid @RequestBody NaturalLanguageQueryRequest request) {
        return queryService.answer(request.question());
    }
}
