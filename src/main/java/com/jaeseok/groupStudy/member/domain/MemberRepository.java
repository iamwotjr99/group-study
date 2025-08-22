package com.jaeseok.groupStudy.member.domain;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long userId);
    Optional<Member> findByEmail(String email);
    boolean existByNickname(String nickname);
    boolean existByEmail(String email);
}
