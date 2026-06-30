package com.example.leadmqlsql.controller;

import com.example.leadmqlsql.dto.DashboardDtos.FunnelResponse;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/funnel")
    public FunnelResponse funnel() {
        return dashboardService.funnel();
    }

    @GetMapping("/anomalies")
    public List<LeadResponse> anomalies() {
        return dashboardService.anomalies();
    }
}
