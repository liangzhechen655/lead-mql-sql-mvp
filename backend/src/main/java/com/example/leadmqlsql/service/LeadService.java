package com.example.leadmqlsql.service;

import com.example.leadmqlsql.dto.LeadDtos.AssignRequest;
import com.example.leadmqlsql.dto.LeadDtos.FollowUpRequest;
import com.example.leadmqlsql.dto.LeadDtos.FollowUpResponse;
import com.example.leadmqlsql.dto.LeadDtos.ImportResult;
import com.example.leadmqlsql.dto.LeadDtos.LeadCreateRequest;
import com.example.leadmqlsql.dto.LeadDtos.LeadDetailResponse;
import com.example.leadmqlsql.dto.LeadDtos.LeadResponse;
import com.example.leadmqlsql.dto.LeadDtos.OperationLogResponse;
import com.example.leadmqlsql.dto.LeadDtos.SalesUserResponse;
import com.example.leadmqlsql.dto.LeadDtos.StatusHistoryResponse;
import com.example.leadmqlsql.dto.LeadDtos.StatusUpdateRequest;
import com.example.leadmqlsql.model.CallStatus;
import com.example.leadmqlsql.model.FollowUpRecord;
import com.example.leadmqlsql.model.ImportBatch;
import com.example.leadmqlsql.model.LeadGrade;
import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.OperationLog;
import com.example.leadmqlsql.model.SalesLead;
import com.example.leadmqlsql.model.SalesUser;
import com.example.leadmqlsql.model.StatusHistory;
import com.example.leadmqlsql.model.ThirdPartyCallbackLog;
import com.example.leadmqlsql.model.WechatStatus;
import com.example.leadmqlsql.repository.FollowUpRecordRepository;
import com.example.leadmqlsql.repository.ImportBatchRepository;
import com.example.leadmqlsql.repository.OperationLogRepository;
import com.example.leadmqlsql.repository.SalesLeadRepository;
import com.example.leadmqlsql.repository.SalesUserRepository;
import com.example.leadmqlsql.repository.StatusHistoryRepository;
import com.example.leadmqlsql.repository.ThirdPartyCallbackLogRepository;
import jakarta.persistence.criteria.JoinType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LeadService {
    private static final Map<LeadStatus, Set<LeadStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(LeadStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(LeadStatus.NEW, EnumSet.of(LeadStatus.PENDING_CALL, LeadStatus.VALID, LeadStatus.CALLED_NOT_CONNECTED, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.PENDING_CALL, EnumSet.of(LeadStatus.CALLED_CONNECTED, LeadStatus.CALLED_NOT_CONNECTED, LeadStatus.VALID, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.CALLED_CONNECTED, EnumSet.of(LeadStatus.VALID, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.CALLED_NOT_CONNECTED, EnumSet.of(LeadStatus.PENDING_CALL, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.VALID, EnumSet.of(LeadStatus.PENDING_WECHAT, LeadStatus.WECHAT_ADDED, LeadStatus.WECHAT_FAILED, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.INVALID, EnumSet.noneOf(LeadStatus.class));
        ALLOWED_TRANSITIONS.put(LeadStatus.PENDING_WECHAT, EnumSet.of(LeadStatus.WECHAT_ADDED, LeadStatus.WECHAT_FAILED));
        ALLOWED_TRANSITIONS.put(LeadStatus.WECHAT_ADDED, EnumSet.of(LeadStatus.MQL, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.WECHAT_FAILED, EnumSet.of(LeadStatus.PENDING_WECHAT, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.FOLLOWING, EnumSet.of(LeadStatus.MQL, LeadStatus.INVALID));
        ALLOWED_TRANSITIONS.put(LeadStatus.MQL, EnumSet.of(LeadStatus.SQL));
        ALLOWED_TRANSITIONS.put(LeadStatus.SQL, EnumSet.noneOf(LeadStatus.class));
    }

    private final SalesLeadRepository leadRepository;
    private final SalesUserRepository userRepository;
    private final FollowUpRecordRepository followUpRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final OperationLogRepository operationLogRepository;
    private final ImportBatchRepository importBatchRepository;
    private final ThirdPartyCallbackLogRepository callbackLogRepository;

    public LeadService(
            SalesLeadRepository leadRepository,
            SalesUserRepository userRepository,
            FollowUpRecordRepository followUpRepository,
            StatusHistoryRepository statusHistoryRepository,
            OperationLogRepository operationLogRepository,
            ImportBatchRepository importBatchRepository,
            ThirdPartyCallbackLogRepository callbackLogRepository
    ) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.followUpRepository = followUpRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.operationLogRepository = operationLogRepository;
        this.importBatchRepository = importBatchRepository;
        this.callbackLogRepository = callbackLogRepository;
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> list(String status, String source, Long ownerId, String keyword, Long currentUserId) {
        SalesUser currentUser = resolveCurrentUser(currentUserId);
        Long effectiveOwnerId = isManager(currentUser) ? ownerId : currentUser.getId();
        Specification<SalesLead> spec = (root, query, cb) -> {
            root.fetch("owner", JoinType.LEFT);
            query.distinct(true);
            return cb.conjunction();
        };
        if (status != null && !status.isBlank()) {
            LeadStatus leadStatus = LeadStatus.valueOf(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), leadStatus));
        }
        if (source != null && !source.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("source"), source));
        }
        if (effectiveOwnerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("owner").get("id"), effectiveOwnerId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("customerName")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(root.get("channel")), like)
            ));
        }
        return leadRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(SalesLead::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toLeadResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeadDetailResponse detail(Long id, Long currentUserId) {
        SalesLead lead = getLead(id);
        assertCanRead(lead, resolveCurrentUser(currentUserId));
        List<FollowUpResponse> followUps = followUpRepository.findByLeadIdOrderByCreatedAtDesc(id).stream()
                .map(this::toFollowUpResponse)
                .toList();
        List<StatusHistoryResponse> histories = statusHistoryRepository.findByLeadIdOrderByChangedAtDesc(id).stream()
                .map(h -> new StatusHistoryResponse(h.getId(), h.getFromStatus(), h.getToStatus(), h.getOperatorName(), h.getReason(), h.getChangedAt()))
                .toList();
        List<OperationLogResponse> logs = operationLogRepository.findByLeadIdOrderByCreatedAtDesc(id).stream()
                .map(l -> new OperationLogResponse(l.getId(), l.getAction(), l.getOperatorName(), l.getDetail(), l.getCreatedAt()))
                .toList();
        return new LeadDetailResponse(toLeadResponse(lead), followUps, histories, logs);
    }

    @Transactional
    public LeadResponse create(LeadCreateRequest request) {
        String phone = cleanPhone(request.phone());
        if (leadRepository.existsByPhone(phone)) {
            throw new BusinessException("Phone already exists. Leads are deduplicated by phone.");
        }
        SalesLead lead = new SalesLead();
        lead.setCustomerName(request.customerName().trim());
        lead.setPhone(phone);
        lead.setSource(request.source().trim());
        lead.setChannel(defaultText(request.channel(), request.source()));
        lead.setGrade(request.grade() == null ? LeadGrade.UNKNOWN : request.grade());
        lead.setRemark(request.remark());
        SalesLead saved = leadRepository.save(lead);
        log(saved, "CREATE_LEAD", "system", "Created lead: " + saved.getCustomerName());
        return toLeadResponse(saved);
    }

    @Transactional
    public LeadResponse updateStatus(Long id, StatusUpdateRequest request) {
        SalesLead lead = getLead(id);
        LeadStatus from = lead.getStatus();
        LeadStatus to = request.status();
        validateTransition(lead, to);
        applyDerivedFields(lead, to, request.invalidReason());
        lead.setStatus(to);
        SalesLead saved = leadRepository.save(lead);
        String operatorName = defaultText(request.operatorName(), "system");
        saveHistory(saved, from, to, operatorName, defaultText(request.reason(), "Manual status change"));
        log(saved, "CHANGE_STATUS", operatorName, from + " -> " + to);
        return toLeadResponse(saved);
    }

    @Transactional
    public LeadResponse assign(Long id, AssignRequest request) {
        SalesLead lead = getLead(id);
        SalesUser owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new NotFoundException("Owner not found"));
        lead.setOwner(owner);
        lead.setAssignedAt(LocalDateTime.now());
        SalesLead saved = leadRepository.save(lead);
        log(saved, "ASSIGN_OWNER", defaultText(request.operatorName(), "manager"), "Assigned to: " + owner.getName());
        return toLeadResponse(saved);
    }

    @Transactional
    public FollowUpResponse addFollowUp(Long leadId, FollowUpRequest request) {
        SalesLead lead = getLead(leadId);
        SalesUser operator = request.operatorId() == null
                ? lead.getOwner()
                : userRepository.findById(request.operatorId()).orElseThrow(() -> new NotFoundException("Operator not found"));
        FollowUpRecord record = new FollowUpRecord();
        record.setLead(lead);
        record.setOperator(operator);
        record.setMethod(request.method());
        record.setContent(request.content());
        record.setNextFollowUpAt(request.nextFollowUpAt());
        FollowUpRecord saved = followUpRepository.save(record);
        lead.setLastFollowUpAt(LocalDateTime.now());
        leadRepository.save(lead);
        log(lead, "ADD_FOLLOW_UP", operatorName(operator), request.method() + ": " + request.content());
        return toFollowUpResponse(saved);
    }

    @Transactional
    public ImportResult importCsv(MultipartFile file, String operatorName) {
        int total = 0;
        int duplicate = 0;
        int failed = 0;
        List<SalesLead> toSave = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (first) {
                    first = false;
                    if (isHeaderRow(line)) {
                        continue;
                    }
                }
                total++;
                String[] cells = line.split(",", -1);
                if (cells.length < 3) {
                    failed++;
                    continue;
                }
                String name = stripBom(cells[0]).trim();
                String phone = cleanPhone(cells[1]);
                String source = cells[2].trim();
                String channel = cells.length > 3 ? cells[3].trim() : source;
                if (name.isBlank() || phone.isBlank() || source.isBlank()) {
                    failed++;
                    continue;
                }
                if (leadRepository.existsByPhone(phone) || toSave.stream().anyMatch(l -> l.getPhone().equals(phone))) {
                    duplicate++;
                    continue;
                }
                SalesLead lead = new SalesLead();
                lead.setCustomerName(name);
                lead.setPhone(phone);
                lead.setSource(source);
                lead.setChannel(channel.isBlank() ? source : channel);
                lead.setStatus(LeadStatus.NEW);
                lead.setImportedAt(LocalDateTime.now());
                toSave.add(lead);
            }
        } catch (Exception ex) {
            throw new BusinessException("CSV import failed: " + ex.getMessage());
        }
        List<SalesLead> saved = leadRepository.saveAll(toSave);
        ImportBatch batch = new ImportBatch();
        batch.setFileName(file.getOriginalFilename() == null ? "leads.csv" : file.getOriginalFilename());
        batch.setTotalRows(total);
        batch.setInsertedRows(saved.size());
        batch.setDuplicateRows(duplicate);
        batch.setFailedRows(failed);
        batch.setOperatorName(defaultText(operatorName, "system"));
        ImportBatch savedBatch = importBatchRepository.save(batch);
        saved.forEach(lead -> log(lead, "IMPORT_LEAD", batch.getOperatorName(), "CSV batch #" + savedBatch.getId()));
        return new ImportResult(savedBatch.getId(), total, saved.size(), duplicate, failed);
    }

    @Transactional
    public LeadResponse applyCallCallback(Long leadId, boolean connected, boolean valid, String invalidReason, String rawResult) {
        SalesLead lead = getLead(leadId);
        lead.setCallStatus(connected ? CallStatus.CONNECTED : CallStatus.NOT_CONNECTED);
        LeadStatus target = connected ? (valid ? LeadStatus.VALID : LeadStatus.INVALID) : LeadStatus.CALLED_NOT_CONNECTED;
        LeadStatus from = lead.getStatus();
        validateTransition(lead, target);
        applyDerivedFields(lead, target, invalidReason);
        lead.setStatus(target);
        SalesLead saved = leadRepository.save(lead);
        saveHistory(saved, from, target, "call-center", defaultText(rawResult, "Mock call result callback"));
        saveCallback(saved, "CALL_CENTER", "CALL_RESULT", "SUCCESS", rawResult);
        log(saved, "CALLBACK_CALL", "call-center", "connected=" + connected + ", valid=" + valid);
        return toLeadResponse(saved);
    }

    @Transactional
    public LeadResponse applyWechatCallback(Long leadId, boolean added, String failedReason, String externalUserId) {
        SalesLead lead = getLead(leadId);
        LeadStatus from = lead.getStatus();
        LeadStatus target = added ? LeadStatus.WECHAT_ADDED : LeadStatus.WECHAT_FAILED;
        validateTransition(lead, target);
        lead.setWechatStatus(added ? WechatStatus.ADDED : WechatStatus.FAILED);
        lead.setStatus(target);
        if (!added) {
            lead.setInvalidReason(failedReason);
        }
        SalesLead saved = leadRepository.save(lead);
        saveHistory(saved, from, target, "wechat-scrm", added ? "Wechat added: " + externalUserId : "Wechat failed: " + failedReason);
        saveCallback(saved, "WECHAT_SCRM", "WECHAT_RESULT", "SUCCESS", "externalUserId=" + externalUserId + ", failedReason=" + failedReason);
        log(saved, "CALLBACK_WECHAT", "wechat-scrm", added ? "Wechat added" : "Wechat failed");
        return toLeadResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> listCallbackCandidates() {
        Specification<SalesLead> spec = (root, query, cb) -> {
            root.fetch("owner", JoinType.LEFT);
            query.distinct(true);
            return cb.conjunction();
        };
        return leadRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(SalesLead::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toLeadResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalesUserResponse> listSalesUsers() {
        return userRepository.findAll().stream()
                .map(u -> new SalesUserResponse(u.getId(), u.getName(), u.getRole(), u.getTeam()))
                .toList();
    }

    public LeadResponse toLeadResponse(SalesLead lead) {
        SalesUser owner = lead.getOwner();
        return new LeadResponse(
                lead.getId(),
                lead.getCustomerName(),
                lead.getPhone(),
                lead.getSource(),
                lead.getChannel(),
                lead.getStatus(),
                lead.getStatus().getLabel(),
                lead.getCallStatus(),
                lead.getWechatStatus(),
                lead.getGrade(),
                owner == null ? null : owner.getId(),
                owner == null ? null : owner.getName(),
                lead.getInvalidReason(),
                lead.getRemark(),
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.getAssignedAt(),
                lead.getLastFollowUpAt(),
                lead.getMqlAt(),
                lead.getSqlAt()
        );
    }

    private FollowUpResponse toFollowUpResponse(FollowUpRecord record) {
        return new FollowUpResponse(
                record.getId(),
                operatorName(record.getOperator()),
                record.getMethod(),
                record.getContent(),
                record.getNextFollowUpAt(),
                record.getCreatedAt()
        );
    }

    private SalesLead getLead(Long id) {
        return leadRepository.findById(id).orElseThrow(() -> new NotFoundException("Lead not found"));
    }

    private SalesUser resolveCurrentUser(Long currentUserId) {
        if (currentUserId == null) {
            throw new BusinessException("Missing X-User-Id. The backend must identify the current user before returning leads.");
        }
        return userRepository.findById(currentUserId).orElseThrow(() -> new BusinessException("Current user not found"));
    }

    private void assertCanRead(SalesLead lead, SalesUser currentUser) {
        if (isManager(currentUser)) {
            return;
        }
        if (lead.getOwner() == null || !currentUser.getId().equals(lead.getOwner().getId())) {
            throw new BusinessException("Sales users can only read leads assigned to themselves.");
        }
    }

    private boolean isManager(SalesUser user) {
        return "MANAGER".equalsIgnoreCase(user.getRole());
    }

    private void validateTransition(SalesLead lead, LeadStatus to) {
        LeadStatus from = lead.getStatus();
        if (from == to) {
            return;
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
            throw new BusinessException("Illegal status transition: " + from + " -> " + to);
        }
        if (to == LeadStatus.SQL && lead.getOwner() == null) {
            throw new BusinessException("SQL requires an assigned owner.");
        }
        if (to == LeadStatus.MQL && lead.getLastFollowUpAt() == null) {
            throw new BusinessException("MQL requires at least one follow-up record.");
        }
    }

    private void applyDerivedFields(SalesLead lead, LeadStatus to, String invalidReason) {
        if (to == LeadStatus.PENDING_CALL) {
            lead.setCallStatus(CallStatus.PENDING);
        }
        if (to == LeadStatus.CALLED_CONNECTED) {
            lead.setCallStatus(CallStatus.CONNECTED);
        }
        if (to == LeadStatus.CALLED_NOT_CONNECTED) {
            lead.setCallStatus(CallStatus.NOT_CONNECTED);
        }
        if (to == LeadStatus.VALID) {
            lead.setCallStatus(CallStatus.CONNECTED);
            lead.setInvalidReason(null);
        }
        if (to == LeadStatus.INVALID) {
            lead.setInvalidReason(defaultText(invalidReason, "Invalid after qualification"));
        }
        if (to == LeadStatus.PENDING_WECHAT) {
            lead.setWechatStatus(WechatStatus.PENDING);
        }
        if (to == LeadStatus.WECHAT_ADDED) {
            lead.setWechatStatus(WechatStatus.ADDED);
        }
        if (to == LeadStatus.WECHAT_FAILED) {
            lead.setWechatStatus(WechatStatus.FAILED);
            lead.setInvalidReason(defaultText(invalidReason, "Wechat add failed"));
        }
        if (to == LeadStatus.MQL && lead.getMqlAt() == null) {
            lead.setMqlAt(LocalDateTime.now());
        }
        if (to == LeadStatus.SQL && lead.getSqlAt() == null) {
            lead.setSqlAt(LocalDateTime.now());
        }
    }

    private void saveHistory(SalesLead lead, LeadStatus from, LeadStatus to, String operatorName, String reason) {
        StatusHistory history = new StatusHistory();
        history.setLead(lead);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setOperatorName(operatorName);
        history.setReason(reason);
        statusHistoryRepository.save(history);
    }

    private void log(SalesLead lead, String action, String operatorName, String detail) {
        OperationLog log = new OperationLog();
        log.setLead(lead);
        log.setAction(action);
        log.setOperatorName(defaultText(operatorName, "system"));
        log.setDetail(detail);
        operationLogRepository.save(log);
    }

    private void saveCallback(SalesLead lead, String systemName, String eventType, String result, String payload) {
        ThirdPartyCallbackLog log = new ThirdPartyCallbackLog();
        log.setLead(lead);
        log.setSystemName(systemName);
        log.setEventType(eventType);
        log.setResult(result);
        log.setPayload(payload);
        callbackLogRepository.save(log);
    }

    private boolean isHeaderRow(String line) {
        String normalized = stripBom(line).toLowerCase(Locale.ROOT);
        return normalized.contains("phone")
                || normalized.contains("mobile")
                || normalized.contains("tel")
                || normalized.contains("\u7535\u8BDD")
                || normalized.contains("\u624B\u673A")
                || normalized.contains("\u59D3\u540D,\u7535\u8BDD")
                || normalized.contains("\u59D3\u540D,\u624B\u673A");
    }

    private String cleanPhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9+]", "");
    }

    private String stripBom(String value) {
        return value == null ? "" : value.replace("\uFEFF", "");
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String operatorName(SalesUser user) {
        return Optional.ofNullable(user).map(SalesUser::getName).orElse("system");
    }
}
