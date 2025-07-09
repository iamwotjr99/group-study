package com.jaeseok.groupStudy.user.domain.vo;

import java.util.Objects;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Password)) {
            return false;
        }
        Password other = (Password) o;
        return Objects.equals(encodedValue, other.encodedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(encodedValue);
    }
}
