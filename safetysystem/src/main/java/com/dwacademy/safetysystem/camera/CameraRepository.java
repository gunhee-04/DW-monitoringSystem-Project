package com.dwacademy.safetysystem.camera;


import org.springframework.data.jpa.repository.JpaRepository;
import com.dwacademy.safetysystem.camera.Camera;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
    
    //활성화된 카메라만 조회
    List<Camera> findByStatus(String status);

    Optional<Camera> findByCameraCode(String cameraCode);
}
