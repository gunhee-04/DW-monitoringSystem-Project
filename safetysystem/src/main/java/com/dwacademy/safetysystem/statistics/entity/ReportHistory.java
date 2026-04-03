package com.dwacademy.safetysystem.statistics.entity;

import com.dwacademy.safetysystem.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "report_history",
        indexes = {
                @Index(name = "idx_report_history_member_id", columnList = "member_id"),
                @Index(name = "idx_report_history_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
public class ReportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}