package com.example.leadmqlsql.repository;

import com.example.leadmqlsql.model.ThirdPartyCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThirdPartyCallbackLogRepository extends JpaRepository<ThirdPartyCallbackLog, Long> {
}
