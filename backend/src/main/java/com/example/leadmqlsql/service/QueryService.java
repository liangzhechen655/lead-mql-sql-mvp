package com.example.leadmqlsql.service;

import com.example.leadmqlsql.dto.DashboardDtos.ChannelConversion;
import com.example.leadmqlsql.dto.DashboardDtos.FunnelResponse;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.dto.QueryDtos.NaturalLanguageQueryResponse;
import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.SalesLead;
import com.example.leadmqlsql.repository.SalesLeadRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    private final DashboardService dashboardService;
    private final SalesLeadRepository leadRepository;

    public QueryService(DashboardService dashboardService, SalesLeadRepository leadRepository) {
        this.dashboardService = dashboardService;
        this.leadRepository = leadRepository;
    }

    public NaturalLanguageQueryResponse answer(String question) {
        String normalized = question == null ? "" : question.trim();
        boolean asksCurrentWeek = normalized.contains("\u672C\u5468") || normalized.toLowerCase().contains("this week");
        FunnelResponse funnel = dashboardService.funnel();
        if (containsChannelSqlTopQuestion(normalized)) {
            ChannelConversion best = asksCurrentWeek ? bestWeeklyChannelConversion() : bestAllTimeChannelConversion(funnel);
            String scope = asksCurrentWeek ? "\u672C\u5468" : "\u5F53\u524D\u5168\u91CF";
            String answer = best == null
                    ? scope + "\u6CA1\u6709\u8DB3\u591F\u7EBF\u7D22\u6570\u636E\uFF0C\u65E0\u6CD5\u8BA1\u7B97\u6E20\u9053 SQL \u8F6C\u5316\u7387\u3002"
                    : scope + " SQL \u8F6C\u5316\u7387\u6700\u9AD8\u7684\u6E20\u9053\u662F " + best.channel()
                    + "\uFF0CSQL \u6570 " + best.sqlCount() + "/" + best.total()
                    + "\uFF0C\u8F6C\u5316\u7387 " + best.sqlRate() + "%\u3002";
            return new NaturalLanguageQueryResponse(question, answer, asksCurrentWeek ? "WEEKLY_CHANNEL_SQL_RATE_TOP" : "CHANNEL_SQL_RATE_TOP");
        }
        if (normalized.contains("\u5F02\u5E38") || normalized.contains("\u8D85\u65F6") || normalized.contains("48")) {
            List<LeadResponse> anomalies = dashboardService.anomalies();
            return new NaturalLanguageQueryResponse(question, "\u5F53\u524D\u5F02\u5E38/\u8D85\u65F6\u672A\u8DDF\u8FDB\u7EBF\u7D22\u5171 " + anomalies.size() + " \u6761\u3002", "ANOMALY_COUNT");
        }
        if (normalized.contains("\u6F0F\u6597") || normalized.contains("\u8F6C\u5316\u7387") || normalized.toUpperCase().contains("MQL")) {
            String answer = "\u5F53\u524D\u603B\u7EBF\u7D22 " + funnel.total()
                    + " \u6761\uFF0C\u6709\u6548\u7387 " + funnel.validRate()
                    + "%\uFF0C\u52A0\u5FAE\u7387 " + funnel.wechatRate()
                    + "%\uFF0CMQL \u8F6C\u5316\u7387 " + funnel.mqlRate()
                    + "%\uFF0CSQL \u8F6C\u5316\u7387 " + funnel.sqlRate() + "%\u3002";
            return new NaturalLanguageQueryResponse(question, answer, "FUNNEL_SUMMARY");
        }
        return new NaturalLanguageQueryResponse(question, "\u6682\u672A\u547D\u4E2D\u89C4\u5219\u3002\u4F60\u53EF\u4EE5\u95EE\uFF1A\u672C\u5468\u54EA\u4E2A\u6E20\u9053 SQL \u8F6C\u5316\u7387\u6700\u9AD8\uFF1F\u5F53\u524D\u6709\u591A\u5C11\u5F02\u5E38\u7EBF\u7D22\uFF1F\u6574\u4F53\u6F0F\u6597\u8F6C\u5316\u7387\u662F\u591A\u5C11\uFF1F", "FALLBACK_HELP");
    }

    private boolean containsChannelSqlTopQuestion(String normalized) {
        return normalized.contains("\u6E20\u9053")
                && normalized.toUpperCase().contains("SQL")
                && (normalized.contains("\u6700\u9AD8") || normalized.contains("\u6700\u597D"));
    }

    private ChannelConversion bestAllTimeChannelConversion(FunnelResponse funnel) {
        return funnel.channelConversions().isEmpty() ? null : funnel.channelConversions().getFirst();
    }

    private ChannelConversion bestWeeklyChannelConversion() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = monday.atStartOfDay();
        List<SalesLead> weeklyLeads = leadRepository.findAll().stream()
                .filter(lead -> lead.getCreatedAt() != null && !lead.getCreatedAt().isBefore(weekStart))
                .toList();
        if (weeklyLeads.isEmpty()) {
            return null;
        }
        Map<String, List<SalesLead>> byChannel = weeklyLeads.stream()
                .collect(Collectors.groupingBy(lead -> lead.getChannel() == null ? lead.getSource() : lead.getChannel()));
        return byChannel.entrySet().stream()
                .map(entry -> {
                    long total = entry.getValue().size();
                    long sqlCount = entry.getValue().stream()
                            .filter(lead -> lead.getStatus() == LeadStatus.SQL && lead.getSqlAt() != null && !lead.getSqlAt().isBefore(weekStart))
                            .count();
                    return new ChannelConversion(entry.getKey(), total, sqlCount, rate(sqlCount, total));
                })
                .max(Comparator.comparingDouble(ChannelConversion::sqlRate).thenComparing(ChannelConversion::sqlCount))
                .orElse(null);
    }

    private double rate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0;
        }
        return Math.round((numerator * 10000.0 / denominator)) / 100.0;
    }
}
