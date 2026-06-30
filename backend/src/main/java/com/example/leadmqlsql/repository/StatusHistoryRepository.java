package com.example.leadmqlsql.repository;

import com.example.leadmqlsql.model.StatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByLeadIdOrderByChangedAtDesc(Long leadId);
}
