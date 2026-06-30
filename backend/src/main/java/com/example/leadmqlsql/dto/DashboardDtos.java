package com.example.leadmqlsql.dto;

import java.util.List;
import java.util.Map;

public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record FunnelResponse(
            long total,
            Map<String, Long> counts,
            double validRate,
            double wechatRate,
            double mqlRate,
            double sqlRate,
            List<ChannelConversion> channelConversions
    ) {
    }

    public record ChannelConversion(
            String channel,
            long total,
            long sqlCount,
            double sqlRate
    ) {
    }
}
