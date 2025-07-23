package com.study.spring.domain.member.repository;

import com.study.spring.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    List<Member> findByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nickname, String email);
} 