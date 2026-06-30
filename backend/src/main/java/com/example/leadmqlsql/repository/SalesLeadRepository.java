package com.example.leadmqlsql.repository;

import com.example.leadmqlsql.model.LeadStatus;
import com.example.leadmqlsql.model.SalesLead;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SalesLeadRepository extends JpaRepository<SalesLead, Long>, JpaSpecificationExecutor<SalesLead> {
    Optional<SalesLead> findByPhone(String phone);

    boolean existsByPhone(String phone);

    long countByStatus(LeadStatus status);

    long countByStatusIn(Collection<LeadStatus> statuses);

    List<SalesLead> findByStatusInAndLastFollowUpAtBefore(Collection<LeadStatus> statuses, LocalDateTime threshold);

    List<SalesLead> findByStatusInAndLastFollowUpAtIsNull(Collection<LeadStatus> statuses);
}
