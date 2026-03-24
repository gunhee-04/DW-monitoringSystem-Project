package com.dwacademy.safetysystem.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {

    List<AlertLog> findTop10ByOrderByCreatedAtDesc();
}