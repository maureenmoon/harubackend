package com.study.spring.domain.member.service;

import com.study.spring.domain.member.dto.MemberDto;
import com.study.spring.domain.member.dto.MemberDto.MultipartRequest;
import com.study.spring.domain.member.entity.Member;
import com.study.spring.domain.member.entity.Role;
import com.study.spring.domain.member.repository.MemberRepository;
import com.study.spring.domain.member.util.FileUploadUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final FileUploadUtil fileUploadUtil;
    
    	@Transactional
    public MemberDto.Response createMemberWithImage(MemberDto.MultipartRequest request, MultipartFile profileImage) {
       // 이메일 중복 확인
       if (memberRepository.existsByEmail(request.getEmail())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
    }
    
    // 닉네임 중복 확인
    if (memberRepository.existsByNickname(request.getNickname())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다.");
    }
    
    // 이미지 저장
    String profileImageUrl = null;
    
    if (profileImage != null && !profileImage.isEmpty()) {
        profileImageUrl = fileUploadUtil.saveFile(profileImage); // <- save image
    }
    
    System.out.println("프로필 이미지 업로드 완료: " + profileImageUrl);
      
    // Member 엔티티 생성 (편의 메서드 사용)
        Member member = Member.createMember()
                .email(request.getEmail())
                .password(request.getPassword()) // 실제로는 암호화 필요
                .nickname(request.getNickname())
                .name(request.getName())
                .birthAt(request.getBirthAt())
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .activityLevel(request.getActivityLevel())
                .profileImageUrl(profileImageUrl)
                .role(Role.USER) //set default role
                .build();
        
        
        return MemberDto.Response.from(memberRepository.save(member));
    }

    public MemberDto.Response getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        
        return MemberDto.Response.from(member);
    }

    public MemberDto.Response getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        
        return MemberDto.Response.from(member);
    }

    public MemberDto.Response getMemberByNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
        
        return MemberDto.Response.from(member);
    }

    public MemberDto.Response authenticateByNickname(String nickname, String password) {
        try {
            Member member = memberRepository.findByNickname(nickname)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

            // Debug log
            System.out.println("입력된 비밀번호: " + password);
            System.out.println("DB 비밀번호: " + member.getPassword());

            if (!member.getPassword().equals(password)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
            }

            return MemberDto.Response.from(member);
        } catch (Exception e) {
            e.printStackTrace();  // This will print the actual cause to the server logs
            throw e;  // Rethrow to preserve behavior
        }
    }

    @Transactional
    public MemberDto.Response updateMemberWithImage(Long id, MemberDto.MultipartRequest request, MultipartFile imageFile) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        // 이메일 변경 시 중복 확인
        if (!member.getEmail().equals(request.getEmail()) && memberRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");
        }
        
        // 닉네임 변경 시 중복 확인
        if (!member.getNickname().equals(request.getNickname()) && memberRepository.existsByNickname(request.getNickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다.");
        }
        
        String imageUrl = member.getProfileImageUrl();//keep exisiting if no new image
        
        if (imageFile != null && !imageFile.isEmpty()) {
        	imageUrl = fileUploadUtil.saveFile(imageFile);
        }

        System.out.println("프로필 이미지 업로드 완료: " + imageUrl);
        
       // Member 엔티티 업데이트 (편의 메서드 사용)
       Member updateMember = member.toBuilder()
       .email(request.getEmail())
       .password(request.getPassword()) // 실제로는 암호화 필요
       .nickname(request.getNickname())
       .name(request.getName())
       .birthAt(request.getBirthAt())
       .gender(request.getGender())
       .height(request.getHeight())
       .weight(request.getWeight())
       .activityLevel(request.getActivityLevel())
       .profileImageUrl(imageUrl)
       .build();

		//Member updatedMember = memberRepository.save(member);
		return MemberDto.Response.from(memberRepository.save(updateMember));
}

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // toBuilder() 사용으로 통일
        Member updatedMember = member.toBuilder()
                .password(newPassword) // 실제로는 암호화 필요
                .build();

        memberRepository.save(updatedMember);
    }

    @Transactional
    public void updateProfileImage(Long id, MultipartFile profileImage) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        String imageUrl = fileUploadUtil.saveFile(profileImage);
        
        // toBuilder() 사용으로 통일
        Member updatedMember = member.toBuilder()
                .profileImageUrl(imageUrl)
                .build();

        memberRepository.save(updatedMember);
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

        memberRepository.delete(member);
    }

    // 추가 편의 메서드들
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    public List<MemberDto.Response> searchMembers(String query) {
        List<Member> members = memberRepository.findByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        return members.stream().map(MemberDto.Response::from).toList();
    }


}