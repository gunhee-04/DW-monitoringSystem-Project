package com.dwacademy.safetysystem.statistics.repository;

import com.dwacademy.safetysystem.statistics.entity.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {
    List<ReportHistory> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}