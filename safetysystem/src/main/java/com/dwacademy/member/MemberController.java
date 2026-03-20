package com.dwacademy.member;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/list")
    public String list (){
        return "member/list";
    }

    @GetMapping("/createMember")
    public String createMember() {
        return "member/createMember";
    }

    @PostMapping("createMemberProc")
    public String createMemberProc(){
        return "redirect:/member/list";
    }

    @GetMapping("/updateMember")
    public String updateMember(){
        return "member/updateMember";
    }

    @PostMapping("updateMemberProc")
    public String updateMemberProc(){
        return "redirect:/member/list";
    }

    @GetMapping("/deleteMember")
    public String deleteMember(){
        return "member/deleteMember";
    }

    @PostMapping("deleteMemberProc")
    public String deleteMemberProc(){
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(){
        return "member/login";
    }


}
