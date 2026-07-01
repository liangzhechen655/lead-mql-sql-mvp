package com.example.leadmqlsql.service;

import com.example.leadmqlsql.model.CallStatus;
import com.example.leadmqlsql.model.FollowUpRecord;
import com.example.leadmqlsql.model.LeadGrade;
import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.SalesLead;
import com.example.leadmqlsql.model.SalesUser;
import com.example.leadmqlsql.model.StatusHistory;
import com.example.leadmqlsql.model.WechatStatus;
import com.example.leadmqlsql.repository.FollowUpRecordRepository;
import com.example.leadmqlsql.repository.SalesLeadRepository;
import com.example.leadmqlsql.repository.SalesUserRepository;
import com.example.leadmqlsql.repository.StatusHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final SalesUserRepository userRepository;
    private final SalesLeadRepository leadRepository;
    private final FollowUpRecordRepository followUpRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(
            SalesUserRepository userRepository,
            SalesLeadRepository leadRepository,
            FollowUpRecordRepository followUpRepository,
            StatusHistoryRepository statusHistoryRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.followUpRepository = followUpRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        backfillVersionForExistingLeads();

        SalesUser zhang = ensureUser("张三", "SALES", "华东销售组");
        SalesUser li = ensureUser("李四", "SALES", "华南销售组");
        ensureUser("王主管", "MANAGER", "增长运营部");

        List<SeedLead> seeds = List.of(
                new SeedLead("陈先生", "13800000001", "百度投放", "百度-搜索", LeadStatus.NEW, null, LeadGrade.B, null),
                new SeedLead("周女士", "13800000002", "抖音投放", "抖音-直播间", LeadStatus.VALID, zhang, LeadGrade.A, LocalDateTime.now().minusHours(60)),
                new SeedLead("林同学", "13800000003", "官网表单", "官网", LeadStatus.WECHAT_ADDED, li, LeadGrade.A, LocalDateTime.now().minusHours(5)),
                new SeedLead("赵经理", "13800000004", "微信社群", "社群", LeadStatus.MQL, zhang, LeadGrade.A, LocalDateTime.now().minusHours(3)),
                new SeedLead("吴老师", "13800000005", "百度投放", "百度-信息流", LeadStatus.SQL, li, LeadGrade.A, LocalDateTime.now().minusHours(2)),
                new SeedLead("无效样例", "13800000006", "抖音投放", "抖音-短视频", LeadStatus.INVALID, null, LeadGrade.C, null),
                new SeedLead("何先生", "13800000007", "小红书投放", "小红书-搜索", LeadStatus.PENDING_CALL, null, LeadGrade.B, null),
                new SeedLead("刘女士", "13800000008", "百度投放", "百度-搜索", LeadStatus.CALLED_CONNECTED, zhang, LeadGrade.B, LocalDateTime.now().minusHours(20)),
                new SeedLead("孙总", "13800000009", "官网表单", "官网-预约咨询", LeadStatus.CALLED_NOT_CONNECTED, null, LeadGrade.C, null),
                new SeedLead("马先生", "13800000010", "抖音投放", "抖音-表单", LeadStatus.PENDING_WECHAT, li, LeadGrade.A, LocalDateTime.now().minusHours(18)),
                new SeedLead("高女士", "13800000011", "微信社群", "社群-直播课", LeadStatus.WECHAT_FAILED, zhang, LeadGrade.B, LocalDateTime.now().minusHours(30)),
                new SeedLead("黄先生", "13800000012", "合作渠道", "代理商-A", LeadStatus.FOLLOWING, li, LeadGrade.A, LocalDateTime.now().minusHours(8)),
                new SeedLead("宋女士", "13800000013", "百度投放", "百度-信息流", LeadStatus.SQL, zhang, LeadGrade.A, LocalDateTime.now().minusHours(4)),
                new SeedLead("郑先生", "13800000014", "小红书投放", "小红书-笔记", LeadStatus.MQL, li, LeadGrade.B, LocalDateTime.now().minusHours(6)),
                new SeedLead("唐女士", "13800000015", "官网表单", "官网-资料下载", LeadStatus.VALID, zhang, LeadGrade.B, LocalDateTime.now().minusHours(74)),
                new SeedLead("许经理", "13800000016", "合作渠道", "代理商-B", LeadStatus.WECHAT_ADDED, li, LeadGrade.A, LocalDateTime.now().minusHours(54)),
                new SeedLead("邓先生", "13800000017", "抖音投放", "抖音-直播间", LeadStatus.FOLLOWING, zhang, LeadGrade.A, LocalDateTime.now().minusHours(52)),
                new SeedLead("曹女士", "13800000018", "微信社群", "社群-转介绍", LeadStatus.NEW, null, LeadGrade.C, null),
                new SeedLead("彭先生", "13800000019", "百度投放", "百度-搜索", LeadStatus.PENDING_CALL, null, LeadGrade.B, null),
                new SeedLead("梁女士", "13800000020", "小红书投放", "小红书-搜索", LeadStatus.VALID, li, LeadGrade.A, LocalDateTime.now().minusHours(12)),
                new SeedLead("罗先生", "13800000021", "官网表单", "官网-在线客服", LeadStatus.SQL, zhang, LeadGrade.A, LocalDateTime.now().minusHours(1)),
                new SeedLead("方女士", "13800000022", "合作渠道", "代理商-A", LeadStatus.INVALID, null, LeadGrade.C, null),
                new SeedLead("程先生", "13800000023", "抖音投放", "抖音-短视频", LeadStatus.WECHAT_ADDED, zhang, LeadGrade.B, LocalDateTime.now().minusHours(9)),
                new SeedLead("薛女士", "13800000024", "微信社群", "社群-公开课", LeadStatus.FOLLOWING, li, LeadGrade.A, LocalDateTime.now().minusHours(7))
        );

        seeds.forEach(this::createLeadIfMissing);
    }

    private void backfillVersionForExistingLeads() {
        jdbcTemplate.update("update lead_record set version = 0 where version is null");
    }

    private SalesUser ensureUser(String name, String role, String team) {
        return userRepository.findAll().stream()
                .filter(user -> user.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    SalesUser user = new SalesUser();
                    user.setName(name);
                    user.setRole(role);
                    user.setTeam(team);
                    return userRepository.save(user);
                });
    }

    private void createLeadIfMissing(SeedLead seed) {
        if (leadRepository.existsByPhone(seed.phone())) {
            return;
        }

        SalesLead lead = new SalesLead();
        lead.setCustomerName(seed.name());
        lead.setPhone(seed.phone());
        lead.setSource(seed.source());
        lead.setChannel(seed.channel());
        lead.setStatus(seed.status());
        lead.setOwner(seed.owner());
        lead.setGrade(seed.grade());
        lead.setAssignedAt(seed.owner() == null ? null : LocalDateTime.now().minusDays(1));
        lead.setLastFollowUpAt(seed.lastFollowUpAt());
        applyDerivedFields(lead, seed.status());

        SalesLead saved = leadRepository.save(lead);
        createStatusHistory(saved, seed.status());
        if (seed.lastFollowUpAt() != null) {
            createFollowUp(saved, seed.owner());
        }
    }

    private void applyDerivedFields(SalesLead lead, LeadStatus status) {
        if (status == LeadStatus.CALLED_CONNECTED
                || status == LeadStatus.VALID
                || status == LeadStatus.PENDING_WECHAT
                || status == LeadStatus.WECHAT_ADDED
                || status == LeadStatus.WECHAT_FAILED
                || status == LeadStatus.FOLLOWING
                || status == LeadStatus.MQL
                || status == LeadStatus.SQL) {
            lead.setCallStatus(CallStatus.CONNECTED);
        }
        if (status == LeadStatus.CALLED_NOT_CONNECTED) {
            lead.setCallStatus(CallStatus.NOT_CONNECTED);
        }
        if (status == LeadStatus.WECHAT_ADDED || status == LeadStatus.FOLLOWING || status == LeadStatus.MQL || status == LeadStatus.SQL) {
            lead.setWechatStatus(WechatStatus.ADDED);
        }
        if (status == LeadStatus.WECHAT_FAILED) {
            lead.setWechatStatus(WechatStatus.FAILED);
            lead.setInvalidReason("客户暂不愿添加企微");
        }
        if (status == LeadStatus.MQL || status == LeadStatus.SQL) {
            lead.setMqlAt(LocalDateTime.now().minusHours(12));
        }
        if (status == LeadStatus.SQL) {
            lead.setSqlAt(LocalDateTime.now().minusHours(1));
        }
        if (status == LeadStatus.INVALID) {
            lead.setInvalidReason("空号或无业务兴趣");
        }
    }

    private void createStatusHistory(SalesLead saved, LeadStatus status) {
        StatusHistory history = new StatusHistory();
        history.setLead(saved);
        history.setFromStatus(null);
        history.setToStatus(status);
        history.setOperatorName("初始化数据");
        history.setReason("模拟演示数据");
        statusHistoryRepository.save(history);
    }

    private void createFollowUp(SalesLead saved, SalesUser owner) {
        FollowUpRecord record = new FollowUpRecord();
        record.setLead(saved);
        record.setOperator(owner);
        record.setMethod("电话");
        record.setContent("模拟跟进：确认需求、预算和下一步动作。");
        followUpRepository.save(record);
    }

    private record SeedLead(
            String name,
            String phone,
            String source,
            String channel,
            LeadStatus status,
            SalesUser owner,
            LeadGrade grade,
            LocalDateTime lastFollowUpAt
    ) {
    }
}
