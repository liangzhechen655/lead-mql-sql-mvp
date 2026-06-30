package com.example.leadmqlsql.repository;

import com.example.leadmqlsql.model.OperationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    List<OperationLog> findByLeadIdOrderByCreatedAtDesc(Long leadId);
}
