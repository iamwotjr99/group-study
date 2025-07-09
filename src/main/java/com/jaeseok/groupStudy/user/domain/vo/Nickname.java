package com.jaeseok.groupStudy.user.domain.vo;

import java.util.Objects;

public record Nickname(String value) {

    public Nickname {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        if (value.length() < 5) {
            throw new IllegalArgumentException("닉네임이 너무 짧습니다.");
        }

        if (value.length() > 10) {
            throw new IllegalArgumentException("닉네임은 10자 이하로 설정해주세요");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nickname)) return false;
        Nickname other = (Nickname) o;
        return this.value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
