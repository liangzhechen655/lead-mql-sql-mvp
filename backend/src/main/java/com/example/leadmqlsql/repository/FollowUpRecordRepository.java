package com.example.leadmqlsql.repository;

import com.example.leadmqlsql.model.FollowUpRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowUpRecordRepository extends JpaRepository<FollowUpRecord, Long> {
    List<FollowUpRecord> findByLeadIdOrderByCreatedAtDesc(Long leadId);
}
