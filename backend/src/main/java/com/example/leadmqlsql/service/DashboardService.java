package com.example.leadmqlsql.service;

import com.example.leadmqlsql.dto.DashboardDtos.ChannelConversion;
import com.example.leadmqlsql.dto.DashboardDtos.FunnelResponse;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.SalesLead;
import com.example.leadmqlsql.repository.SalesLeadRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final SalesLeadRepository leadRepository;
    private final LeadService leadService;

    public DashboardService(SalesLeadRepository leadRepository, LeadService leadService) {
        this.leadRepository = leadRepository;
        this.leadService = leadService;
    }

    @Transactional(readOnly = true)
    public FunnelResponse funnel() {
        List<SalesLead> leads = leadRepository.findAll();
        long total = leads.size();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (LeadStatus status : LeadStatus.values()) {
            counts.put(status.name(), leads.stream().filter(l -> l.getStatus() == status).count());
        }
        long valid = countIn(leads, LeadStatus.VALID, LeadStatus.PENDING_WECHAT, LeadStatus.WECHAT_ADDED, LeadStatus.WECHAT_FAILED, LeadStatus.MQL, LeadStatus.SQL);
        long wechatAdded = countIn(leads, LeadStatus.WECHAT_ADDED, LeadStatus.MQL, LeadStatus.SQL);
        long mql = countIn(leads, LeadStatus.MQL, LeadStatus.SQL);
        long sql = countIn(leads, LeadStatus.SQL);
        List<ChannelConversion> channelConversions = leads.stream()
                .collect(Collectors.groupingBy(l -> l.getChannel() == null ? l.getSource() : l.getChannel()))
                .entrySet()
                .stream()
                .map(e -> {
                    long channelTotal = e.getValue().size();
                    long channelSql = e.getValue().stream().filter(l -> l.getStatus() == LeadStatus.SQL).count();
                    return new ChannelConversion(e.getKey(), channelTotal, channelSql, rate(channelSql, channelTotal));
                })
                .sorted((a, b) -> Double.compare(b.sqlRate(), a.sqlRate()))
                .toList();
        return new FunnelResponse(total, counts, rate(valid, total), rate(wechatAdded, valid), rate(mql, total), rate(sql, total), channelConversions);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> anomalies() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        List<LeadStatus> activeStatuses = List.of(LeadStatus.VALID, LeadStatus.PENDING_WECHAT, LeadStatus.WECHAT_ADDED, LeadStatus.MQL);
        Map<Long, SalesLead> result = new LinkedHashMap<>();
        leadRepository.findByStatusInAndLastFollowUpAtBefore(activeStatuses, threshold)
                .forEach(lead -> result.put(lead.getId(), lead));
        leadRepository.findByStatusInAndLastFollowUpAtIsNull(activeStatuses)
                .forEach(lead -> result.put(lead.getId(), lead));
        return result.values().stream().map(leadService::toLeadResponse).toList();
    }

    private long countIn(List<SalesLead> leads, LeadStatus... statuses) {
        Map<LeadStatus, Boolean> target = new EnumMap<>(LeadStatus.class);
        for (LeadStatus status : statuses) {
            target.put(status, true);
        }
        return leads.stream().filter(l -> target.containsKey(l.getStatus())).count();
    }

    private double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0;
        }
        return Math.round((numerator * 10000.0 / denominator)) / 100.0;
    }
}
