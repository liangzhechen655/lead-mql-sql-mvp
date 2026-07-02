package com.example.leadmqlsql;

import com.example.leadmqlsql.dto.LeadDtos.AssignRequest;
import com.example.leadmqlsql.dto.LeadDtos.FollowUpRequest;
import com.example.leadmqlsql.dto.LeadDtos.LeadCreateRequest;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.dto.LeadDtos.StatusUpdateRequest;
import com.example.leadmqlsql.model.LeadGrade;
import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.SalesUser;
import com.example.leadmqlsql.repository.SalesUserRepository;
import com.example.leadmqlsql.service.BusinessException;
import com.example.leadmqlsql.service.DashboardService;
import com.example.leadmqlsql.service.LeadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class LeadServiceTests {
    @Autowired
    private LeadService leadService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SalesUserRepository userRepository;

    @Test
    void rejectsDuplicatePhone() {
        LeadCreateRequest request = new LeadCreateRequest("Test Lead", "13900000001", "Website", "Website", LeadGrade.B, "test");
        leadService.create(request);

        assertThatThrownBy(() -> leadService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Phone already exists");
    }

    @Test
    void rejectsIllegalStatusJump() {
        LeadResponse lead = leadService.create(new LeadCreateRequest("Jump Lead", "13900000002", "Douyin", "Douyin", LeadGrade.A, ""));

        assertThatThrownBy(() -> leadService.updateStatus(lead.id(), new StatusUpdateRequest(LeadStatus.MQL, "jump", "tester", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Illegal status transition");
    }

    @Test
    void salesUserCannotBypassOwnerFilter() {
        SalesUser sales = findUserByRole("SALES");
        leadService.create(new LeadCreateRequest("Unassigned Lead", "13900000003", "Website", "Website", LeadGrade.B, ""));

        assertThat(leadService.list(null, null, null, null, sales.getId()))
                .allMatch(lead -> sales.getId().equals(lead.ownerId()));
    }

    @Test
    void canCompleteCoreMqlSqlFlow() {
        SalesUser sales = findUserByRole("SALES");
        LeadResponse lead = leadService.create(new LeadCreateRequest("Flow Lead", "13900000004", "Baidu", "Baidu Search", LeadGrade.A, ""));
        leadService.assign(lead.id(), new AssignRequest(sales.getId(), "Manager"));
        leadService.updateStatus(lead.id(), new StatusUpdateRequest(LeadStatus.VALID, "call valid", "call-center", null));
        leadService.updateStatus(lead.id(), new StatusUpdateRequest(LeadStatus.WECHAT_ADDED, "wechat added", "wechat", null));
        leadService.addFollowUp(lead.id(), new FollowUpRequest(sales.getId(), "Phone", "Customer has clear interest", null));
        LeadResponse afterFollowUp = leadService.detail(lead.id(), sales.getId()).lead();
        assertThat(afterFollowUp.status()).isEqualTo(LeadStatus.WECHAT_ADDED);
        LeadResponse mql = leadService.updateStatus(lead.id(), new StatusUpdateRequest(LeadStatus.MQL, "qualified", sales.getName(), null));
        LeadResponse sql = leadService.updateStatus(lead.id(), new StatusUpdateRequest(LeadStatus.SQL, "sales opportunity", sales.getName(), null));

        assertThat(mql.mqlAt()).isNotNull();
        assertThat(sql.sqlAt()).isNotNull();
        assertThat(dashboardService.funnel().total()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void validLeadCanMoveToWechatStages() {
        LeadResponse lead = leadService.create(new LeadCreateRequest("Wechat Lead", "13900000005", "Website", "Website", LeadGrade.A, ""));
        LeadResponse valid = leadService.updateStatus(lead.id(), new StatusUpdateRequest(LeadStatus.VALID, "call valid", "call-center", null));
        LeadResponse pendingWechat = leadService.updateStatus(valid.id(), new StatusUpdateRequest(LeadStatus.PENDING_WECHAT, "prepare add wechat", "operator", null));
        LeadResponse wechatAdded = leadService.updateStatus(pendingWechat.id(), new StatusUpdateRequest(LeadStatus.WECHAT_ADDED, "wechat added", "operator", null));

        assertThat(valid.status()).isEqualTo(LeadStatus.VALID);
        assertThat(pendingWechat.status()).isEqualTo(LeadStatus.PENDING_WECHAT);
        assertThat(wechatAdded.status()).isEqualTo(LeadStatus.WECHAT_ADDED);
    }

    private SalesUser findUserByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .findFirst()
                .orElseThrow();
    }
}
