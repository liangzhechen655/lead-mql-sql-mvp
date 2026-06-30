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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final SalesUserRepository userRepository;
    private final SalesLeadRepository leadRepository;
    private final FollowUpRecordRepository followUpRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public DataInitializer(
            SalesUserRepository userRepository,
            SalesLeadRepository leadRepository,
            FollowUpRecordRepository followUpRepository,
            StatusHistoryRepository statusHistoryRepository
    ) {
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.followUpRepository = followUpRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createUser("张三", "SALES", "华东销售组");
            createUser("李四", "SALES", "华南销售组");
            createUser("王主管", "MANAGER", "增长运营部");
        }
        if (leadRepository.count() == 0) {
            List<SalesUser> users = userRepository.findAll();
            SalesUser zhang = users.stream().filter(u -> u.getName().equals("张三")).findFirst().orElse(users.getFirst());
            SalesUser li = users.stream().filter(u -> u.getName().equals("李四")).findFirst().orElse(users.getFirst());
            createLead("陈先生", "13800000001", "百度投放", "百度-搜索", LeadStatus.NEW, null, LeadGrade.B, null);
            createLead("周女士", "13800000002", "抖音投放", "抖音-直播间", LeadStatus.VALID, zhang, LeadGrade.A, LocalDateTime.now().minusHours(60));
            createLead("林同学", "13800000003", "官网表单", "官网", LeadStatus.WECHAT_ADDED, li, LeadGrade.A, LocalDateTime.now().minusHours(5));
            createLead("赵经理", "13800000004", "微信社群", "社群", LeadStatus.MQL, zhang, LeadGrade.A, LocalDateTime.now().minusHours(3));
            createLead("吴老师", "13800000005", "百度投放", "百度-信息流", LeadStatus.SQL, li, LeadGrade.A, LocalDateTime.now().minusHours(2));
            createLead("无效样例", "13800000006", "抖音投放", "抖音-短视频", LeadStatus.INVALID, null, LeadGrade.C, null);
        }
    }

    private void createUser(String name, String role, String team) {
        SalesUser user = new SalesUser();
        user.setName(name);
        user.setRole(role);
        user.setTeam(team);
        userRepository.save(user);
    }

    private void createLead(String name, String phone, String source, String channel, LeadStatus status, SalesUser owner, LeadGrade grade, LocalDateTime lastFollowUpAt) {
        SalesLead lead = new SalesLead();
        lead.setCustomerName(name);
        lead.setPhone(phone);
        lead.setSource(source);
        lead.setChannel(channel);
        lead.setStatus(status);
        lead.setOwner(owner);
        lead.setGrade(grade);
        lead.setAssignedAt(owner == null ? null : LocalDateTime.now().minusDays(1));
        lead.setLastFollowUpAt(lastFollowUpAt);
        if (status == LeadStatus.VALID || status == LeadStatus.WECHAT_ADDED || status == LeadStatus.MQL || status == LeadStatus.SQL) {
            lead.setCallStatus(CallStatus.CONNECTED);
        }
        if (status == LeadStatus.WECHAT_ADDED || status == LeadStatus.MQL || status == LeadStatus.SQL) {
            lead.setWechatStatus(WechatStatus.ADDED);
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
        SalesLead saved = leadRepository.save(lead);
        StatusHistory history = new StatusHistory();
        history.setLead(saved);
        history.setFromStatus(null);
        history.setToStatus(status);
        history.setOperatorName("初始化数据");
        history.setReason("模拟演示数据");
        statusHistoryRepository.save(history);
        if (lastFollowUpAt != null) {
            FollowUpRecord record = new FollowUpRecord();
            record.setLead(saved);
            record.setOperator(owner);
            record.setMethod("电话");
            record.setContent("模拟跟进：确认需求和预算，记录下一步动作。");
            followUpRepository.save(record);
        }
    }
}
