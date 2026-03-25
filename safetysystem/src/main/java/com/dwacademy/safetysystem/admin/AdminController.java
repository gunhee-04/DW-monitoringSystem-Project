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
        Camera camera = cameraService.findById(id);

        model.addAttribute("dto", cameraService.toDto(camera));


        return "camera/detail";
    }

    @GetMapping("/camera/create")
    public String cameraCreate(Model model) {
        model.addAttribute("dto", new CameraDto());
        return "camera/create";
    }

    @PostMapping("/camera/createProc")
    public String cameraCreateProc(@Valid @ModelAttribute("dto") CameraDto dto,
                                   BindingResult result) {

        System.out.println("===== 카메라 등록 요청 들어옴 =====");
        System.out.println("dto = " + dto);

        if (result.hasErrors()) {
            System.out.println("===== 검증 오류 발생 =====");
            System.out.println(result.getAllErrors());
            return "camera/create";
        }

        System.out.println("===== 저장 시작 =====");
        cameraService.save(dto);
        System.out.println("===== 저장 완료 =====");

        return "redirect:/admin/camera/list";
    }

    @GetMapping("/camera/{id}/update")
    public String cameraUpdate(@PathVariable Long id, Model model) {
        Camera camera = cameraService.findById(id);
        model.addAttribute("dto", cameraService.toDto(camera));
        return "camera/update";
    }

    @PostMapping("/camera/{id}/updateProc")
    public String cameraUpdateProc(@PathVariable Long id,
                                   @Valid @ModelAttribute("dto") CameraDto dto,
                                   BindingResult result) {

        if (result.hasErrors()) {
            return "camera/update";
        }

        cameraService.update(id, dto);
        return "redirect:/admin/camera/detail/" + id;
    }

    @PostMapping("/camera/{id}/delete")
    public String cameraDeleteProc(@PathVariable Long id) {
        cameraService.softDelete(id);
        return "redirect:/admin/camera/list";
    }

    // =========================
    // DangerZone
    // =========================

    @GetMapping("/zone/{cameraId}")
    public String zoneList(@PathVariable Long cameraId, Model model) {
        model.addAttribute("list", zoneService.findByCamera(cameraId));
        model.addAttribute("cameraId", cameraId);
        return "zone/list";
    }

    @GetMapping("/zone/detail/{id}")
    public String zoneDetail(@PathVariable Long id, Model model) {
        DangerZone zone = zoneService.findById(id);
        model.addAttribute("dto", zoneService.toDto(zone));
        return "zone/detail";
    }

    @GetMapping("/zone/{cameraId}/create")
    public String zoneCreate(@PathVariable Long cameraId, Model model) {

        DangerZoneDto dto = new DangerZoneDto();
        dto.setCameraId(cameraId);

        Camera camera = cameraService.findById(cameraId);

        model.addAttribute("dto", dto);
        model.addAttribute("streamUrl", camera.getStreamUrl());

        return "zone/create";
    }

    @PostMapping("/zone/createProc")
    public String zoneCreateProc(@Valid @ModelAttribute("dto") DangerZoneDto dto,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            Camera camera = cameraService.findById(dto.getCameraId());
            model.addAttribute("streamUrl", camera.getStreamUrl());
            return "zone/create";
        }

        zoneService.save(dto);
        return "redirect:/admin/zone/" + dto.getCameraId();
    }

    @GetMapping("/zone/{id}/update")
    public String zoneUpdate(@PathVariable Long id, Model model) {

        DangerZone zone = zoneService.findById(id);
        Camera camera = cameraService.findById(zone.getCameraId());

        model.addAttribute("dto", zoneService.toDto(zone));
        model.addAttribute("streamUrl", camera.getStreamUrl());

        return "zone/update";
    }

    @PostMapping("/zone/{id}/updateProc")
    public String zoneUpdateProc(@PathVariable Long id,
                                 @Valid @ModelAttribute("dto") DangerZoneDto dto,
                                 BindingResult result,
                                 Model model) {

        if (result.hasErrors()) {
            Camera camera = cameraService.findById(dto.getCameraId());
            model.addAttribute("streamUrl", camera.getStreamUrl());
            return "zone/update";
        }

        zoneService.update(id, dto);
        return "redirect:/admin/zone/" + dto.getCameraId();
    }

    @PostMapping("/zone/{id}/delete")
    public String zoneDeleteProc(@PathVariable Long id,
                                 @RequestParam Long cameraId) {
        zoneService.softDelete(id);
        return "redirect:/admin/zone/" + cameraId;
    }
}