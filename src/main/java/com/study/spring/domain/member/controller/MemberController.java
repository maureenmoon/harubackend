package com.study.spring.domain.member.controller;

import com.study.spring.domain.member.dto.MemberDto;
import com.study.spring.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 회원 가입+프로필 이미지 생성
    @PostMapping(value = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberDto.Response> createMemberWithImage(
    		@RequestPart("data") MemberDto.MultipartRequest request,
    		@RequestPart(value="profileImage", required = false) MultipartFile profileImage) {
    	return ResponseEntity.ok(memberService.createMemberWithImage(request, profileImage));
    }
   
    // 로그인 (닉네임 기반)
    @PostMapping("/login")
    public ResponseEntity<MemberDto.Response> login(@RequestBody MemberDto.LoginRequest request) {
        return ResponseEntity.ok(memberService.authenticateByNickname(request.getNickname(), request.getPassword()));
    }

    // ID로 회원 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberDto.Response> getMember(@PathVariable("id") Long id) {
        return ResponseEntity.ok(memberService.getMember(id));
    }

    // 닉네임으로 회원 조회
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<MemberDto.Response> getMemberByNickname(@PathVariable("nickname") String nickname) {
        return ResponseEntity.ok(memberService.getMemberByNickname(nickname));
    }

    // 이메일로 회원 조회
    @GetMapping("/email/{email}")
    public ResponseEntity<MemberDto.Response> getMemberByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(memberService.getMemberByEmail(email));
    }

    // 회원 정보 수정
    @PutMapping(value = "/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemberDto.Response> updateMemberWithImage(
//            @PathVariable("id") Long id,
            @PathVariable Long id,
            @RequestPart("data") MemberDto.MultipartRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        return ResponseEntity.ok(memberService.updateMemberWithImage(id, request, profileImage));
    }
    
    // 비밀번호 변경
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable("id") Long id,
            @RequestParam String newPassword) {
        memberService.updatePassword(id, newPassword);
        return ResponseEntity.noContent().build();
    }
    
    		
    // 프로필 이미지 변경
    @PatchMapping(value = "/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfileImage(
            @PathVariable("id") Long id,
            @RequestPart("profileImage") MultipartFile profileImage) {
        memberService.updateProfileImage(id, profileImage);
        return ResponseEntity.noContent().build();
    }
    // 회원 탈퇴
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("id") Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam("email") String email) {
        return ResponseEntity.ok(memberService.existsByEmail(email));
    }

    // 닉네임 중복 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNicknameExists(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(memberService.existsByNickname(nickname));
    }

    // 프로필 검색
    @GetMapping("/search")
    public ResponseEntity<List<MemberDto.Response>> searchMembers(@RequestParam("query") String query) {
        return ResponseEntity.ok(memberService.searchMembers(query));
    }
} 