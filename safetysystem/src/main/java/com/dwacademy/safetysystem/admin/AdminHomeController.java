package com.dwacademy.safetysystem.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    // /admin -> /admin/home 리다이렉트
    @GetMapping("")
    public String redirectHome() {
        return "redirect:/admin/home";
    }

    // 관리자 홈
    @GetMapping("/home")
    public String adminHome() {
        return "admin/admin_home";
    }
}
