package com.dwacademy.safetysystem.member.controller;

import com.dwacademy.safetysystem.member.dto.MemberDto;
import com.dwacademy.safetysystem.member.service.MemberService;
import com.dwacademy.safetysystem.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // 목록 페이지
    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("--- [MemberController] list() ---");
        Page<Member> memberList = memberService.getMembers(pageable);
        model.addAttribute("memberList", memberList);

        return "member/list";
    }

    // 등록 페이지
    @GetMapping("/create")
    public String createPage() {
        log.info("--- [MemberController] createPage() ---");
        return "member/create";
    }

    // 등록 처리
    @PostMapping("/create")
    public String createMember(
            Model model,
            MemberDto memberDto
    ) {
        log.info("--- [MemberController] createMember() ---");
        int result = memberService.createMember(memberDto);

        if (result > 0) {
            model.addAttribute("errorMsg", "회원 등록 중 오류가 발생했습니다.");
            return "member/create";
        }

        return "redirect:/member/list";
    }

    // 수정 페이지
    @GetMapping("/{id}/edit")
    public String editPage(
            @PathVariable int id,
            Model model
    ) {
        log.info("--- [MemberController] editPage() ---");

        Member member = memberService.getMemberById(id);
        model.addAttribute("member", member);

        return "member/update";
    }

    // 수정 처리
    @PostMapping("/{id}/edit")
    public String updateMember(
            @PathVariable int id,
            Model model,
            MemberDto memberDto
    ) {
        log.info("--- [MemberController] updateMember() ---");

        memberDto.setId(id);

        int result = memberService.updateMember(memberDto);

        if (result > 0) {
            model.addAttribute("errorMsg", "회원 수정 중 오류가 발생했습니다.");
            model.addAttribute("member", memberService.getMemberById(id));
            return "member/updateMember";
        }

        return "redirect:/member/list";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String login() {
        log.info("--- [MemberController] login() ---");

        return "member/login";
    }
}