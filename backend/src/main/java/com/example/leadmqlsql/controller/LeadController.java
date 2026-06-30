package com.example.leadmqlsql.controller;

import com.example.leadmqlsql.dto.LeadDtos.AssignRequest;
import com.example.leadmqlsql.dto.LeadDtos.FollowUpRequest;
import com.example.leadmqlsql.dto.LeadDtos.FollowUpResponse;
import com.example.leadmqlsql.dto.LeadDtos.ImportResult;
import com.example.leadmqlsql.dto.LeadDtos.LeadCreateRequest;
import com.example.leadmqlsql.dto.LeadDtos.LeadDetailResponse;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.dto.LeadDtos.SalesUserResponse;
import com.example.leadmqlsql.dto.LeadDtos.StatusUpdateRequest;
import com.example.leadmqlsql.service.LeadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class LeadController {
    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping("/leads")
    public List<LeadResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String keyword,
            @RequestHeader("X-User-Id") Long currentUserId
    ) {
        return leadService.list(status, source, ownerId, keyword, currentUserId);
    }

    @GetMapping("/leads/{id}")
    public LeadDetailResponse detail(@PathVariable Long id, @RequestHeader("X-User-Id") Long currentUserId) {
        return leadService.detail(id, currentUserId);
    }

    @PostMapping("/leads")
    public LeadResponse create(@Valid @RequestBody LeadCreateRequest request) {
        return leadService.create(request);
    }

    @PatchMapping("/leads/{id}/status")
    public LeadResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return leadService.updateStatus(id, request);
    }

    @PatchMapping("/leads/{id}/assign")
    public LeadResponse assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return leadService.assign(id, request);
    }

    @PostMapping("/leads/{id}/follow-ups")
    public FollowUpResponse addFollowUp(@PathVariable Long id, @Valid @RequestBody FollowUpRequest request) {
        return leadService.addFollowUp(id, request);
    }

    @PostMapping("/leads/import")
    public ImportResult importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String operatorName
    ) {
        return leadService.importCsv(file, operatorName);
    }

    @GetMapping("/sales-users")
    public List<SalesUserResponse> salesUsers() {
        return leadService.listSalesUsers();
    }
}
