package com.dwacademy.safetysystem.alert;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Id;
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

    private String severity;

    private String alertMessage;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}