package com.jaeseok.groupStudy.member.domain.vo;

import org.springframework.security.crypto.password.PasswordEncoder;

public record Password(String encodedValue) {

    public Password {
        if (encodedValue == null || encodedValue.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
    }

    public boolean passwordMatches(String rawValue, PasswordEncoder encoder) {
        return encoder.matches(rawValue, this.encodedValue);
    }
}
