package com.dwacademy.safetysystem.alert;

import com.dwacademy.safetysystem.detection_event.domain.EventLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long detectionEventId;

    private String alertType;

    @Enumerated(EnumType.STRING)
    private EventLevel severity;

    private String alertMessage;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}