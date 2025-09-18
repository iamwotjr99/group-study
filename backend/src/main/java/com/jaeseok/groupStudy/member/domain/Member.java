package com.jaeseok.groupStudy.member.domain;

import com.jaeseok.groupStudy.member.domain.vo.Nickname;
import com.jaeseok.groupStudy.member.domain.vo.MemberInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 역할 - 일반 사용자 (시스템에 로그인한 사람)
 * 책임 - 닉네임, 비밀번호 검증, 닉네임 변경
 * 협력 - 방장, 참여자
 */
@Getter
public class Member {
    private final Long id;
    private final MemberInfo memberInfo;

    private Member(Long id, MemberInfo memberInfo) {
        this.id = id;
        this.memberInfo = memberInfo;
    }

    public static Member createMember(String nickname, String email, String encodedPassword) {
        MemberInfo memberInfo = MemberInfo.of(nickname, email, encodedPassword);
        return new Member(null, memberInfo);
    }

    public static Member from(Long id, MemberInfo memberInfo) {
        return new Member(id, memberInfo);
    }

    public boolean checkPassword(String rawValue, PasswordEncoder encoder) {
        return this.memberInfo.password().passwordMatches(rawValue, encoder);
    }

    public Member changeNickname(String newNicknameValue) {
        Nickname newNickname = new Nickname(newNicknameValue);
        return new Member(this.id , memberInfo.withNickname(newNickname));
    }

    public String getUserInfoEmail() {
        return this.memberInfo.email().value();
    }

    public String getUserInfoNickname() {
        return this.memberInfo.nickname().value();
    }

    public String getUserInfoPassword() {
        return this.memberInfo.password().encodedValue();
    }

}

