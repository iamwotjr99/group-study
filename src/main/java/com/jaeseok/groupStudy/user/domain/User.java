package com.jaeseok.groupStudy.user.domain;

import com.jaeseok.groupStudy.user.domain.vo.Nickname;
import com.jaeseok.groupStudy.user.domain.vo.UserInfo;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 역할 - 일반 사용자 (시스템에 로그인한 사람)
 * 책임 - 닉네임, 비밀번호 검증, 닉네임 변경
 * 협력 - 방장, 참여자
 */
@Getter
public class User {
    private final Long id;
    private final UserInfo userInfo;

    private User(Long id, UserInfo userInfo) {
        this.id = id;
        this.userInfo = userInfo;
    }

    public static User createUser(String email, String nickname, String encodedPassword) {
        UserInfo userInfo = UserInfo.of(email, nickname, encodedPassword);
        return new User(null, userInfo);
    }

    public static User from(Long id, UserInfo userInfo) {
        return new User(id, userInfo);
    }

    public boolean checkPassword(String rawValue, PasswordEncoder encoder) {
        return this.userInfo.password().passwordMatches(rawValue, encoder);
    }

    public User changeNickname(String newNicknameValue) {
        Nickname newNickname = new Nickname(newNicknameValue);
        return new User(this.id ,userInfo.withNickname(newNickname));
    }

    public String getUserInfoEmail() {
        return this.userInfo.email().value();
    }

    public String getUserInfoNickname() {
        return this.userInfo.nickname().value();
    }

    public String getUserInfoPassword() {
        return this.userInfo.password().encodedValue();
    }

}

