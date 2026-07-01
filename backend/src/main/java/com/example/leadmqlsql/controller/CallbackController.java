package com.example.leadmqlsql.controller;

import com.example.leadmqlsql.dto.CallbackDtos.CallResultRequest;
import com.example.leadmqlsql.dto.CallbackDtos.WechatResultRequest;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.service.LeadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/callbacks")
public class CallbackController {
    private final LeadService leadService;

    public CallbackController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping("/candidates")
    public List<LeadResponse> candidates() {
        return leadService.listCallbackCandidates();
    }

    @PostMapping("/call-result")
    public LeadResponse callResult(@Valid @RequestBody CallResultRequest request) {
        return leadService.applyCallCallback(request.leadId(), request.connected(), request.valid(), request.invalidReason(), request.rawResult());
    }

    @PostMapping("/wechat-result")
    public LeadResponse wechatResult(@Valid @RequestBody WechatResultRequest request) {
        return leadService.applyWechatCallback(request.leadId(), request.added(), request.failedReason(), request.externalUserId());
    }
}
