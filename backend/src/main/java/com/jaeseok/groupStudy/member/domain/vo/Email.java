package com.jaeseok.groupStudy.member.domain.vo;

public record Email(String value) {

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (!isValidFormat(value)) {
            throw new IllegalArgumentException("유효하지 않는 이메일입니다.");
        }
    }

    private boolean isValidFormat(String email) {
        return email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}
