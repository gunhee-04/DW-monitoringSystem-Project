package com.dwacademy.safetysystem.admin;

import com.dwacademy.safetysystem.camera.Camera;
import com.dwacademy.safetysystem.camera.CameraService;
import com.dwacademy.safetysystem.dangerzone.DangerZone;
import com.dwacademy.safetysystem.dangerzone.DangerZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
        private final CameraService cameraService;
        private final DangerZoneService zoneService;

        // =========================
        //  Camera
        // =========================

        // 목록
        @GetMapping("/camera/list")
        public String cameraList(Model model) {
            model.addAttribute("list", cameraService.findAll());
            return "camera/list";
        }

        // 상세보기
        @GetMapping("/camera/detail/{id}")
        public String cameraDetail(@PathVariable Long id, Model model) {
            Camera camera = cameraService.findById(id);
            model.addAttribute("dto", cameraService.toDto(camera));
            return "camera/detail";
        }

        // 등록 화면
        @GetMapping("/camera/create")
        public String cameraCreate(Model model) {
            model.addAttribute("dto", new AdminConfigDto());
            return "camera/create";
        }

        // 등록 처리
        @PostMapping("/camera/createProc")
        public String cameraCreateProc(@ModelAttribute AdminConfigDto dto) {
            cameraService.save(dto);
            return "redirect:/admin/camera/list";
        }

        // 수정 화면
        @GetMapping("/camera/{id}/update")
        public String cameraUpdate(@PathVariable Long id, Model model) {
            Camera camera = cameraService.findById(id);
            model.addAttribute("dto", cameraService.toDto(camera));
            return "camera/update";
        }

        // 수정 처리
        @PostMapping("/camera/{id}/updateProc")
        public String cameraUpdateProc(@PathVariable Long id,
                                       @ModelAttribute AdminConfigDto dto) {
            cameraService.update(id, dto);
            return "redirect:/admin/camera/detail/" + id;
        }

        // 소프트 삭제
        @PostMapping("/camera/{id}/delete")
        public String cameraDeleteProc(@PathVariable Long id) {
            cameraService.softDelete(id);
            return "redirect:/admin/camera/list";
        }

        // =========================
        // DangerZone
        // =========================

        // 목록
        @GetMapping("/zone/{cameraId}")
        public String zoneList(@PathVariable Long cameraId, Model model) {
            model.addAttribute("list", zoneService.findByCamera(cameraId));
            model.addAttribute("cameraId", cameraId);
            return "zone/list";
        }

        // 상세보기
        @GetMapping("/zone/detail/{id}")
        public String zoneDetail(@PathVariable Long id, Model model) {
            DangerZone zone = zoneService.findById(id);
            model.addAttribute("dto", zoneService.toDto(zone));
            return "zone/detail";
        }

        // 등록 화면
        @GetMapping("/zone/{cameraId}/create")
        public String zoneCreate(@PathVariable Long cameraId, Model model) {
            AdminConfigDto dto = new AdminConfigDto();
            dto.setCameraId(cameraId);

            Camera camera = cameraService.findById(cameraId);

            model.addAttribute("dto", dto);
            model.addAttribute("streamUrl", camera.getStreamUrl());

            return "zone/create";
        }

        // 등록 처리
        @PostMapping("/zone/createProc")
        public String zoneCreateProc(@ModelAttribute AdminConfigDto dto) {
            zoneService.save(dto);
            return "redirect:/admin/zone/" + dto.getCameraId();
        }

        @GetMapping("/zone/{id}/update")
        public String zoneUpdate(@PathVariable Long id, Model model){

            DangerZone zone = zoneService.findById(id);
            Camera camera = cameraService.findById(zone.getCameraId());

            model.addAttribute("dto", zoneService.toDto(zone));
            model.addAttribute("streamUrl", camera.getStreamUrl()); // 🔥 추가

            return "zone/update";
        }

        // 수정 처리
        @PostMapping("/zone/{id}/updateProc")
        public String zoneUpdateProc(@PathVariable Long id,
                                     @ModelAttribute AdminConfigDto dto) {
            zoneService.update(id, dto);
            return "redirect:/admin/zone/" + dto.getCameraId();
        }

        // 소프트 삭제
        @PostMapping("/zone/{id}/delete")
        public String zoneDeleteProc(@PathVariable Long id,
                                     @RequestParam Long cameraId) {
            zoneService.softDelete(id);
            return "redirect:/admin/zone/" + cameraId;
        }
    }

