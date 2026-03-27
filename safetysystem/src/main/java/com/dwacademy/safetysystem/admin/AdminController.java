package com.dwacademy.safetysystem.admin;

import com.dwacademy.safetysystem.camera.Camera;
import com.dwacademy.safetysystem.camera.CameraDto;
import com.dwacademy.safetysystem.camera.CameraService;
import com.dwacademy.safetysystem.dangerzone.DangerZone;
import com.dwacademy.safetysystem.dangerzone.DangerZoneDto;
import com.dwacademy.safetysystem.dangerzone.DangerZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final CameraService cameraService;
    private final DangerZoneService zoneService;

    // =========================
    // Camera
    // =========================

    @GetMapping("/camera/list")
    public String cameraList(Model model) {
        model.addAttribute("list", cameraService.findAll());
        return "camera/list";
    }

    @GetMapping("/camera/detail/{id}")
    public String cameraDetail(@PathVariable Long id, Model model) {
        try {
            Camera camera = cameraService.findById(id);
            model.addAttribute("dto", cameraService.toDto(camera));
            return "camera/detail";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @GetMapping("/camera/create")
    public String cameraCreate(Model model) {
        model.addAttribute("dto", new CameraDto());
        return "camera/create";
    }

    @PostMapping("/camera/createProc")
    public String cameraCreateProc(@Valid @ModelAttribute("dto") CameraDto dto,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            return "camera/create";
        }

        try {
            cameraService.save(dto);
            return "redirect:/admin/camera/list";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @GetMapping("/camera/{id}/update")
    public String cameraUpdate(@PathVariable Long id, Model model) {
        try {
            Camera camera = cameraService.findById(id);
            model.addAttribute("dto", cameraService.toDto(camera));
            return "camera/update";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @PostMapping("/camera/{id}/updateProc")
    public String cameraUpdateProc(@PathVariable Long id,
                                   @Valid @ModelAttribute("dto") CameraDto dto,
                                   BindingResult result,
                                   Model model) {

        if (result.hasErrors()) {
            return "camera/update";
        }

        try {
            cameraService.update(id, dto);
            return "redirect:/admin/camera/detail/" + id;
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @PostMapping("/camera/{id}/delete")
    public String cameraDeleteProc(@PathVariable Long id, Model model) {
        try {
            cameraService.softDelete(id);
            return "redirect:/admin/camera/list";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    // =========================
    // DangerZone
    // =========================

    @GetMapping("/zone/{cameraId}")
    public String zoneList(@PathVariable Long cameraId, Model model) {
        try {
            model.addAttribute("list", zoneService.findByCamera(cameraId));
            model.addAttribute("cameraId", cameraId);
            return "zone/list";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @GetMapping("/zone/detail/{id}")
    public String zoneDetail(@PathVariable Long id, Model model) {
        try {
            DangerZone zone = zoneService.findById(id);
            model.addAttribute("dto", zoneService.toDto(zone));
            return "zone/detail";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @GetMapping("/zone/{cameraId}/create")
    public String zoneCreate(@PathVariable Long cameraId, Model model) {
        try {
            DangerZoneDto dto = new DangerZoneDto();
            dto.setCameraId(cameraId);

            Camera camera = cameraService.findById(cameraId);

            model.addAttribute("dto", dto);
            model.addAttribute("streamUrl", camera.getStreamUrl());

            return "zone/create";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @PostMapping("/zone/createProc")
    public String zoneCreateProc(@Valid @ModelAttribute("dto") DangerZoneDto dto,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            try {
                Camera camera = cameraService.findById(dto.getCameraId());
                model.addAttribute("streamUrl", camera.getStreamUrl());
            } catch (Exception e) {
                return error(model, e);
            }
            return "zone/create";
        }

        try {
            zoneService.save(dto);
            flaskReload();
            return "redirect:/admin/zone/" + dto.getCameraId();
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @GetMapping("/zone/{id}/update")
    public String zoneUpdate(@PathVariable Long id, Model model) {
        try {
            DangerZone zone = zoneService.findById(id);
            Camera camera = cameraService.findById(zone.getCameraId());

            model.addAttribute("dto", zoneService.toDto(zone));
            model.addAttribute("streamUrl", camera.getStreamUrl());

            return "zone/update";
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @PostMapping("/zone/{id}/updateProc")
    public String zoneUpdateProc(@PathVariable Long id,
                                 @Valid @ModelAttribute("dto") DangerZoneDto dto,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            try {
                Camera camera = cameraService.findById(dto.getCameraId());
                model.addAttribute("streamUrl", camera.getStreamUrl());
            } catch (Exception e) {
                return error(model, e);
            }
            return "zone/update";
        }

        try {
            zoneService.update(id, dto);
            flaskReload();
            return "redirect:/admin/zone/" + dto.getCameraId();
        } catch (Exception e) {
            return error(model, e);
        }
    }

    @PostMapping("/zone/{id}/delete")
    public String zoneDeleteProc(@PathVariable Long id,
                                 @RequestParam Long cameraId,
                                 Model model) {
        try {
            zoneService.softDelete(id);
            flaskReload();
            return "redirect:/admin/zone/" + cameraId;
        } catch (Exception e) {
            return error(model, e);
        }
    }

    private void flaskReload() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject("http://localhost:5001/reload-config", String.class);
            System.out.println("[Flask ROI reload 요청 완료]");
        } catch (Exception e) {
            System.out.println("[Flask reload 실패] " + e.getMessage());
        }
    }

    // =========================
    // 공통 에러 처리
    // =========================

    private String error(Model model, Exception e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/error-page";
    }
}