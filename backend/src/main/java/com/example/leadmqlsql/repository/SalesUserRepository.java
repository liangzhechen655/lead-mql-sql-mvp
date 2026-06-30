package com.example.leadmqlsql.repository;

import com.example.leadmqlsql.model.SalesUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesUserRepository extends JpaRepository<SalesUser, Long> {
    Optional<SalesUser> findByName(String name);
}
