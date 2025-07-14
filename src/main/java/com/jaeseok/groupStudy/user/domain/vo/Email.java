package com.jaeseok.groupStudy.user.domain.vo;

public record Email(String value) {

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (!isValidFormat(value)) {
            throw new IllegalArgumentException("유효하지 않는 이메일입니다.");
        }

        if (value.length() < 5) {
            throw new IllegalArgumentException("이메일 길이가 너무 짧습니다.");
        }
    }

    private boolean isValidFormat(String email) {
        return email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}
