package com.dwacademy.safetysystem.statistics.service;

import com.dwacademy.safetysystem.statistics.entity.ReportHistory;
import com.dwacademy.safetysystem.statistics.repository.ReportHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportHistoryService {

    private final ReportHistoryRepository reportHistoryRepository;

    public ReportHistoryService(ReportHistoryRepository reportHistoryRepository) {
        this.reportHistoryRepository = reportHistoryRepository;
    }

    public ReportHistory save(ReportHistory reportHistory) {
        reportHistory.setCreatedAt(LocalDateTime.now());
        return reportHistoryRepository.save(reportHistory);
    }

    public List<ReportHistory> findAll() {
        return reportHistoryRepository.findAll();
    }

    public List<ReportHistory> findByPeriod(LocalDateTime start, LocalDateTime end) {
        return reportHistoryRepository.findByCreatedAtBetween(start, end);
    }
}