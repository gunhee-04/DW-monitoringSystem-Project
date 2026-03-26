package com.dwacademy.safetysystem.camera;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {

    // 활성 카메라 조회
    List<Camera> findByStatus(String status);

    // 코드 중복 체크
    boolean existsByCameraCode(String cameraCode);

    // 코드로 조회
    Optional<Camera> findByCameraCode(String cameraCode);
}